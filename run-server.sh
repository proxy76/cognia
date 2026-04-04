#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_DIR"

PG_BIN="/opt/homebrew/opt/postgresql@17/bin"
PG_DATA="/opt/homebrew/var/postgresql@17"
DB_NAME="cognia"
DB_USER="cognia"
DB_PASS="cognia"

# ── Start PostgreSQL if not running ─────────────────────────────────
if ! "$PG_BIN/pg_isready" -q 2>/dev/null; then
    echo "Starting PostgreSQL..."
    brew services start postgresql@17
    # Wait for it to be ready
    for i in {1..10}; do
        "$PG_BIN/pg_isready" -q 2>/dev/null && break
        sleep 1
    done
    if ! "$PG_BIN/pg_isready" -q 2>/dev/null; then
        echo "ERROR: PostgreSQL failed to start"
        exit 1
    fi
fi
echo "PostgreSQL is running."

# ── Ensure database and user exist ──────────────────────────────────
if ! "$PG_BIN/psql" -U "$(whoami)" -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" | grep -q 1; then
    echo "Creating user '$DB_USER'..."
    "$PG_BIN/psql" -U "$(whoami)" -d postgres -c "CREATE USER $DB_USER WITH PASSWORD '$DB_PASS' CREATEDB;"
fi

if ! "$PG_BIN/psql" -U "$(whoami)" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1; then
    echo "Creating database '$DB_NAME'..."
    "$PG_BIN/psql" -U "$(whoami)" -d postgres -c "CREATE DATABASE $DB_NAME OWNER $DB_USER;"
fi

# ── Export environment variables ────────────────────────────────────
export COGNIA_DB_URL="jdbc:postgresql://localhost:5432/$DB_NAME"
export COGNIA_DB_USER="$DB_USER"
export COGNIA_DB_PASSWORD="$DB_PASS"
export COGNIA_DB_MAX_POOL_SIZE=10

# ── Kill anything already on :8080 ─────────────────────────────────
EXISTING=$(lsof -ti tcp:8080 2>/dev/null || true)
if [[ -n "$EXISTING" ]]; then
    echo "Killing existing process on :8080..."
    kill -9 $EXISTING 2>/dev/null || true
    sleep 1
fi

echo "Starting Cognia server on http://localhost:8080 ..."
exec ./gradlew server:run -Pdevelopment
