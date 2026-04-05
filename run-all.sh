#!/usr/bin/env bash
set -euo pipefail

# ── Cognia — launch all targets in parallel ──────────────────────────────────
# Usage: ./run-all.sh              (all targets)
#        ./run-all.sh --no-ios     (skip iOS)
#        ./run-all.sh --no-android (skip Android)
#        ./run-all.sh --verbose    (tail full logs instead of error filter)

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

# ── Colors ───────────────────────────────────────────────────────────────────
RED='\033[0;31m'    GREEN='\033[0;32m'
YELLOW='\033[1;33m' BLUE='\033[0;34m'
CYAN='\033[0;36m'   MAGENTA='\033[0;35m'
BOLD='\033[1m'      DIM='\033[2m'
RESET='\033[0m'

# ── Parse flags ──────────────────────────────────────────────────────────────
SKIP_IOS=false  SKIP_ANDROID=false  SKIP_WEB=false  SKIP_SERVER=false
VERBOSE=false

for arg in "$@"; do
    case "$arg" in
        --no-ios)     SKIP_IOS=true ;;
        --no-android) SKIP_ANDROID=true ;;
        --no-web)     SKIP_WEB=true ;;
        --no-server)  SKIP_SERVER=true ;;
        --verbose|-v) VERBOSE=true ;;
        --help|-h)
            echo "Usage: ./run-all.sh [--no-ios] [--no-android] [--no-web] [--no-server] [--verbose]"
            exit 0 ;;
    esac
done

# ── Track child PIDs for cleanup ─────────────────────────────────────────────
PIDS=()
IOS_SIM_UDID=""

cleanup() {
    echo ""
    echo -e "${BOLD}Shutting down...${RESET}"
    for pid in "${PIDS[@]}"; do
        kill -TERM "$pid" 2>/dev/null || true
    done
    lsof -ti tcp:8080 2>/dev/null | xargs kill -9 2>/dev/null || true
    lsof -ti tcp:8081 2>/dev/null | xargs kill -9 2>/dev/null || true
    if [[ -n "$IOS_SIM_UDID" ]]; then
        xcrun simctl shutdown "$IOS_SIM_UDID" 2>/dev/null || true
    fi
    wait 2>/dev/null
    echo -e "${GREEN}All stopped.${RESET}"
    exit 0
}
trap cleanup SIGINT SIGTERM

# ── Log dir ──────────────────────────────────────────────────────────────────
LOG_DIR="$PROJECT_DIR/.run-logs"
rm -rf "$LOG_DIR" && mkdir -p "$LOG_DIR"

# ── Status file — each target writes its state here ─────────────────────────
STATUS_FILE="$LOG_DIR/.status"
touch "$STATUS_FILE"

update_status() {
    local target="$1" state="$2"
    # Atomic: write to temp then move
    local tmp="$STATUS_FILE.tmp.$$"
    grep -v "^$target=" "$STATUS_FILE" 2>/dev/null > "$tmp" || true
    echo "$target=$state" >> "$tmp"
    mv "$tmp" "$STATUS_FILE"
}

# ── Launch helper ────────────────────────────────────────────────────────────
launch() {
    local label="$1" logfile="$2"
    shift 2
    "$@" > "$logfile" 2>&1 &
    local pid=$!
    PIDS+=("$pid")
}

wait_for_url() {
    local url="$1" max="$2"
    local i=0
    while ! curl -sf "$url" >/dev/null 2>&1; do
        sleep 1
        i=$((i+1))
        (( i >= max )) && return 1
    done
    return 0
}

# ── Banner ───────────────────────────────────────────────────────────────────
echo -e "${BOLD}"
echo "  ╔═══════════════════════════════════════╗"
echo "  ║         Cognia — Run All Targets      ║"
echo "  ╚═══════════════════════════════════════╝"
echo -e "${RESET}"

# ══════════════════════════════════════════════════════════════════════════════
# 0. BUILD SERVER + WEB IN ONE GRADLE INVOCATION
# ══════════════════════════════════════════════════════════════════════════════
# Gradle holds a daemon lock, so two concurrent ./gradlew calls block each
# other.  Build everything we need in a single invocation first, then launch
# the artefacts directly.

if [[ "$SKIP_SERVER" == false ]]; then
    EXISTING=$(lsof -ti tcp:8080 2>/dev/null || true)
    if [[ -n "$EXISTING" ]]; then
        echo -e "${CYAN}[SERVER]${RESET} Killing existing :8080"
        kill -9 $EXISTING 2>/dev/null || true
        sleep 1
    fi
fi

# Build the server fat JAR (single Gradle call, no daemon lock issues)
if [[ "$SKIP_SERVER" == false ]]; then
    update_status SERVER "building"
    echo -e "${CYAN}[SERVER]${RESET} Building fat JAR..."
    if ! ./gradlew server:buildFatJar -Pdevelopment > "$LOG_DIR/server-build.log" 2>&1; then
        update_status SERVER "FAILED"
        echo -e "${RED}[SERVER]${RESET} Build failed — see $LOG_DIR/server-build.log"
    fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# 1. SERVER  (run the built JAR directly — frees Gradle for the web task)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_SERVER" == false ]]; then
    SERVER_JAR=$(find "$PROJECT_DIR/server/build/libs" -name '*-all.jar' 2>/dev/null | head -1)
    if [[ -n "$SERVER_JAR" ]]; then
        (
            if java -Dio.ktor.development=true -jar "$SERVER_JAR" > "$LOG_DIR/server.log" 2>&1; then
                update_status SERVER "stopped"
            else
                update_status SERVER "FAILED"
            fi
        ) &
        PIDS+=($!)
    else
        echo -e "${RED}[SERVER]${RESET} Fat JAR not found after build"
        update_status SERVER "FAILED"
    fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# 2. WEB  (Gradle is now free — no daemon lock contention)
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_WEB" == false ]]; then
    update_status WEB "building"
    (
        if ./gradlew composeApp:jsBrowserDevelopmentRun --continuous > "$LOG_DIR/web.log" 2>&1; then
            update_status WEB "stopped"
        else
            update_status WEB "FAILED"
        fi
    ) &
    PIDS+=($!)
fi

# ══════════════════════════════════════════════════════════════════════════════
# 3. ANDROID
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_ANDROID" == false ]]; then
    ADB="$ANDROID_HOME/platform-tools/adb"
    if [[ -f "$ADB" ]]; then
        DEVICE_COUNT=$("$ADB" devices 2>/dev/null | grep -c "device$" || true)
        DEVICE_COUNT=${DEVICE_COUNT:-0}
        if (( DEVICE_COUNT == 0 )); then
            EMULATOR="$ANDROID_HOME/emulator/emulator"
            AVD=""
            if [[ -f "$EMULATOR" ]]; then
                AVD=$("$EMULATOR" -list-avds 2>/dev/null | head -1 || true)
            fi
            if [[ -n "$AVD" ]]; then
                echo -e "${YELLOW}[ANDROID]${RESET} Booting emulator: $AVD"
                "$EMULATOR" -avd "$AVD" -no-snapshot-load >/dev/null 2>&1 &
                PIDS+=($!)
                "$ADB" wait-for-device 2>/dev/null
                for _ in $(seq 1 120); do
                    BOOT=$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)
                    [[ "$BOOT" == "1" ]] && break
                    sleep 1
                done
            else
                echo -e "${YELLOW}[ANDROID]${RESET} ${DIM}No device/AVD — skipped${RESET}"
                SKIP_ANDROID=true
            fi
        fi

        if [[ "$SKIP_ANDROID" == false ]]; then
            update_status ANDROID "building"
            (
                if ./gradlew composeApp:installDebug > "$LOG_DIR/android-build.log" 2>&1; then
                    update_status ANDROID "installing"
                    "$ADB" shell am start -n "com.cognia.app/.MainActivity" > /dev/null 2>&1
                    update_status ANDROID "running"
                else
                    update_status ANDROID "FAILED"
                fi
            ) &
            PIDS+=($!)
        fi
    else
        echo -e "${YELLOW}[ANDROID]${RESET} ${DIM}SDK not found — skipped${RESET}"
        SKIP_ANDROID=true
    fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# 4. iOS
# ══════════════════════════════════════════════════════════════════════════════
if [[ "$SKIP_IOS" == false ]]; then
    IOS_SIM_UDID=$(xcrun simctl list devices available -j 2>/dev/null \
        | python3 -c "
import sys, json
data = json.load(sys.stdin)
for runtime, devices in data.get('devices', {}).items():
    if 'iOS' not in runtime: continue
    for d in devices:
        if 'iPhone' in d.get('name', '') and d.get('isAvailable', False):
            print(d['udid']); sys.exit(0)
" 2>/dev/null || echo "")

    if [[ -n "$IOS_SIM_UDID" ]]; then
        SIM_NAME=$(xcrun simctl list devices available | grep "$IOS_SIM_UDID" | sed 's/ (.*//' | xargs)
        update_status iOS "booting"
        (
            SIM_STATE=$(xcrun simctl list devices -j | python3 -c "
import sys, json
data = json.load(sys.stdin)
for rt, devs in data.get('devices', {}).items():
    for d in devs:
        if d['udid'] == '$IOS_SIM_UDID': print(d['state']); sys.exit(0)
" 2>/dev/null || echo "Unknown")

            if [[ "$SIM_STATE" != "Booted" ]]; then
                xcrun simctl boot "$IOS_SIM_UDID" 2>/dev/null || true
                open -a Simulator --args -CurrentDeviceUDID "$IOS_SIM_UDID"
            fi

            update_status iOS "building"
            # Strip macOS extended attributes that break codesign
            xattr -cr "$PROJECT_DIR/iosApp" 2>/dev/null || true
            xattr -cr "$PROJECT_DIR/build/ios-derived" 2>/dev/null || true
            if xcodebuild \
                -project iosApp/iosApp.xcodeproj \
                -scheme iosApp \
                -configuration Debug \
                -destination "platform=iOS Simulator,id=$IOS_SIM_UDID" \
                -derivedDataPath "$PROJECT_DIR/build/ios-derived" \
                -quiet \
                build \
                > "$LOG_DIR/ios-build.log" 2>&1; then

                update_status iOS "installing"
                APP_PATH=$(find "$PROJECT_DIR/build/ios-derived" -name "iosApp.app" -path "*/Debug-iphonesimulator/*" | head -1)
                if [[ -n "$APP_PATH" ]]; then
                    xcrun simctl install "$IOS_SIM_UDID" "$APP_PATH"
                    BUNDLE_ID=$(defaults read "$APP_PATH/Info.plist" CFBundleIdentifier 2>/dev/null || echo "com.cognia.app.iosApp")
                    xcrun simctl launch "$IOS_SIM_UDID" "$BUNDLE_ID"
                    update_status iOS "running"
                else
                    update_status iOS "FAILED"
                fi
            else
                update_status iOS "FAILED"
            fi
        ) &
        PIDS+=($!)
    else
        echo -e "${GREEN}[iOS]${RESET} ${DIM}No simulator available — skipped${RESET}"
        SKIP_IOS=true
    fi
fi

# ══════════════════════════════════════════════════════════════════════════════
# STATUS DASHBOARD — polls until all targets are up, then watches for errors
# ══════════════════════════════════════════════════════════════════════════════

# Wait for server health
if [[ "$SKIP_SERVER" == false ]]; then
    if wait_for_url "http://localhost:8080/health" 60; then
        update_status SERVER "running"
    else
        update_status SERVER "FAILED"
    fi
fi

# Wait for webpack dev server
if [[ "$SKIP_WEB" == false ]]; then
    if wait_for_url "http://localhost:8081" 90; then
        update_status WEB "running"
    else
        update_status WEB "FAILED"
    fi
fi

# Wait a moment for Android/iOS subshells to finish updating status
sleep 2

# ── Print dashboard ──────────────────────────────────────────────────────────
print_dashboard() {
    echo -e "${BOLD}──────────────────────────────────────────${RESET}"
    while IFS='=' read -r target state; do
        [[ -z "$target" ]] && continue
        case "$state" in
            running)   icon="${GREEN}OK${RESET}" ;;
            building|installing|booting) icon="${YELLOW}..${RESET}" ;;
            FAILED)    icon="${RED}FAIL${RESET}" ;;
            stopped)   icon="${DIM}--${RESET}" ;;
            *)         icon="${DIM}??${RESET}" ;;
        esac
        case "$target" in
            SERVER)  url=" ${DIM}http://localhost:8080${RESET}" ;;
            WEB)     url=" ${DIM}http://localhost:8081${RESET}" ;;
            ANDROID) url="" ;;
            iOS)     url=" ${DIM}${SIM_NAME:-Simulator}${RESET}" ;;
            *)       url="" ;;
        esac
        printf "  [%b] %-10s %b\n" "$icon" "$target" "$url"
    done < "$STATUS_FILE"
    echo -e "${BOLD}──────────────────────────────────────────${RESET}"
}

print_dashboard

# Show any failures
HAS_FAILURE=false
while IFS='=' read -r target state; do
    if [[ "$state" == "FAILED" ]]; then
        HAS_FAILURE=true
        case "$target" in
            SERVER)  logfile="server-build.log" ;;
            WEB)     logfile="web.log" ;;
            ANDROID) logfile="android-build.log" ;;
            iOS)     logfile="ios-build.log" ;;
            *)       logfile="" ;;
        esac
        if [[ -n "$logfile" && -f "$LOG_DIR/$logfile" ]]; then
            echo ""
            echo -e "${RED}── $target ERRORS ──${RESET}"
            # Show the most useful error lines
            grep -iE "FAILURE|ERROR|FAILED|exception|error:" "$LOG_DIR/$logfile" \
                | grep -v "^$" \
                | grep -v "Run with --" \
                | grep -v "Get more help" \
                | tail -15
        fi
    fi
done < "$STATUS_FILE"

if [[ "$HAS_FAILURE" == false ]]; then
    echo -e "\n  ${GREEN}All targets up.${RESET} Watching for errors...  ${DIM}(Ctrl+C to stop)${RESET}\n"
else
    echo -e "\n  ${DIM}Full logs in: $LOG_DIR${RESET}\n"
fi

# ── Live error watcher ───────────────────────────────────────────────────────
# Instead of dumping all output, only surface lines that matter.
if [[ "$VERBOSE" == true ]]; then
    tail -f "$LOG_DIR"/*.log 2>/dev/null &
else
    tail -f "$LOG_DIR"/*.log 2>/dev/null \
        | grep --line-buffered -iE \
            "FAIL|ERROR|exception|BUILD (SUCCESSFUL|FAILED)|Application started|started in|webpack compiled|Listening on|Address already in use|BindException|OOM|OutOfMemory|WARN[^I]" \
        | grep --line-buffered -v \
            "^> Task\|checkKotlinGradle\|SKIPPED\|UP-TO-DATE\|FROM-CACHE\|Run with --\|Get more help\|Configuration cache\|Calculating task\|Type-safe project\|Selector didn't match\|segment:" \
        | while IFS= read -r line; do
            # Prefix with timestamp
            TS=$(date +%H:%M:%S)
            # Color errors red, success green
            if echo "$line" | grep -qiE "FAIL|ERROR|exception|BindException|OOM"; then
                echo -e "${DIM}$TS${RESET} ${RED}$line${RESET}"
            else
                echo -e "${DIM}$TS${RESET} $line"
            fi
        done &
fi
PIDS+=($!)

wait
