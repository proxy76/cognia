# Deployment — Production (Mac Mini)

After development stabilizes on the MacBook, all runtime services migrate to a Mac Mini for production hosting.

---

## Migration Checklist

### 1. Environment Setup
- [ ] Install JDK 17+ on Mac Mini
- [ ] Install FFmpeg on Mac Mini
- [ ] Clone the repository
- [ ] Copy or create `.env` file with production values

### 2. Data Migration
- [ ] Copy SQLite database file from MacBook to Mac Mini
- [ ] Copy video files (raw, processed, thumbnails) to Mac Mini
- [ ] Verify file paths match new environment config
- [ ] Verify database integrity after copy

### 3. Configuration Changes

| Setting | MacBook (dev) | Mac Mini (prod) |
|---------|--------------|-----------------|
| `COGNIA_HOST` | `0.0.0.0` | `0.0.0.0` |
| `COGNIA_PORT` | `8080` | `8080` (or desired) |
| `COGNIA_DB_PATH` | `./data/cognia-dev.db` | `/opt/cognia/data/cognia.db` |
| `COGNIA_VIDEO_*_PATH` | `./data/videos/*` | `/opt/cognia/data/videos/*` |
| `COGNIA_JWT_SECRET` | `dev-secret-...` | **Strong production secret** |
| `COGNIA_JWT_ISSUER` | `cognia-dev` | `cognia` |

### 4. Networking
- [ ] Configure Mac Mini's static local IP or dynamic DNS
- [ ] Open port 8080 (or desired port) on Mac Mini firewall
- [ ] Configure router port forwarding if clients access from outside local network
- [ ] Update client base URL to point to Mac Mini's address
- [ ] Set up HTTPS (reverse proxy with Caddy or nginx + Let's Encrypt)

### 5. Service Management
- [ ] Create a startup script or launchd plist to run the backend on boot
- [ ] Configure automatic restart on crash
- [ ] Set up log rotation

### 6. Verification
- [ ] Backend starts and serves API requests
- [ ] WebSocket connections work (chat, notifications)
- [ ] Video upload, processing, and playback work
- [ ] Mobile clients connect to new backend address
- [ ] Web client loads correctly

---

## Directory Structure on Mac Mini

```
/opt/cognia/
├── app/                    # Application JAR or project checkout
├── data/
│   ├── cognia.db          # SQLite database
│   └── videos/
│       ├── raw/
│       ├── processed/
│       └── thumbnails/
├── logs/
│   └── cognia.log
├── .env                    # Production environment variables
└── scripts/
    ├── start.sh
    ├── stop.sh
    └── backup.sh
```

## Startup Script Example

```bash
#!/bin/bash
# /opt/cognia/scripts/start.sh
set -e

export $(cat /opt/cognia/.env | xargs)

cd /opt/cognia/app
nohup ./gradlew server:run > /opt/cognia/logs/cognia.log 2>&1 &
echo $! > /opt/cognia/cognia.pid
echo "Cognia backend started (PID: $(cat /opt/cognia/cognia.pid))"
```

## Backup Strategy

- **Database:** Regular copy of SQLite file (while backend is stopped or using `.backup` command)
- **Videos:** Periodic rsync of video directories to external storage
- **Frequency:** Daily recommended for active use

## Future Considerations

- Replace SQLite with PostgreSQL if concurrency demands grow
- Add CDN for video delivery if bandwidth becomes a bottleneck
- Containerize with Docker for easier deployment management
- These are not MVP requirements — only pursue if needed
