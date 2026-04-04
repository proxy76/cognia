# Deployment — Development (MacBook)

All services run locally on the development MacBook during initial development and testing.

---

## Services

| Service | Runtime | Default Port |
|---------|---------|-------------|
| Backend (Ktor) | JVM (Gradle run) | 8080 |
| WebSocket (Chat) | Same Ktor process | 8080 (ws://) |
| WebSocket (Notifications) | Same Ktor process | 8080 (ws://) |
| Web client (dev server) | Kotlin/JS dev server | 3000 |

## Database

- **Engine:** SQLite
- **File location:** `./data/cognia-dev.db`
- **Migrations:** Applied on backend startup (embedded migration runner)
- **PRAGMA:** `foreign_keys = ON` enabled on every connection

## Video Storage

- **Raw uploads:** `./data/videos/raw/`
- **Processed videos:** `./data/videos/processed/`
- **Thumbnails:** `./data/videos/thumbnails/`
- Videos are served by the backend via HTTP (no CDN in dev)

## Environment Variables

```env
# Server
COGNIA_PORT=8080
COGNIA_HOST=0.0.0.0
COGNIA_DB_PATH=./data/cognia-dev.db

# JWT
COGNIA_JWT_SECRET=dev-secret-change-in-production
COGNIA_JWT_ISSUER=cognia-dev
COGNIA_JWT_EXPIRATION_MS=3600000
COGNIA_JWT_REFRESH_EXPIRATION_MS=2592000000

# Video
COGNIA_VIDEO_RAW_PATH=./data/videos/raw
COGNIA_VIDEO_PROCESSED_PATH=./data/videos/processed
COGNIA_VIDEO_THUMBNAIL_PATH=./data/videos/thumbnails
COGNIA_VIDEO_MAX_SIZE_MB=500

# OAuth (provide real credentials)
COGNIA_GOOGLE_CLIENT_ID=
COGNIA_APPLE_CLIENT_ID=
COGNIA_APPLE_TEAM_ID=
COGNIA_APPLE_KEY_ID=

# AI (Onboarding)
COGNIA_ANTHROPIC_API_KEY=
COGNIA_ANTHROPIC_MODEL=claude-sonnet-4-20250514

# FFmpeg
COGNIA_FFMPEG_PATH=/opt/homebrew/bin/ffmpeg
```

## Prerequisites

- JDK 17+
- Gradle (wrapper included)
- FFmpeg installed (`brew install ffmpeg`)
- Node.js (for web dev server, if needed)

## Startup Order

1. Ensure `./data/` directory exists
2. Start backend: `./gradlew server:run`
   - Applies DB migrations on startup
   - Starts HTTP + WebSocket listeners on port 8080
3. Start web client (optional): `./gradlew composeApp:wasmJsBrowserDevelopmentRun`
4. Android/iOS: run from IDE (Android Studio / Xcode)

## Mobile Device Testing

- Backend must be accessible from the device
- For local network testing: use MacBook's local IP instead of `localhost`
- Update client base URL configuration accordingly
- Android emulator: use `10.0.2.2:8080` to reach host machine

## Data Reset

To reset all data:
```bash
rm -rf ./data/
# Restart backend (will recreate DB and directories)
```

## Logs

- Backend logs: stdout (via Logback, see `server/src/main/resources/logback.xml`)
- Log level configurable via environment or logback.xml
