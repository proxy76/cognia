# System Architecture

## Overview

Cognia is built on **Kotlin Multiplatform (KMP)** with shared domain models and business logic across Android, iOS, web, and backend. The architecture prioritizes code sharing, MVP simplicity, and practical deployment on a single machine.

```
┌─────────────────────────────────────────────────────┐
│                    Clients                           │
│  ┌─────────┐  ┌─────────┐  ┌─────────────────────┐ │
│  │ Android │  │   iOS   │  │   Web (secondary)   │ │
│  │ Compose │  │  Swift  │  │  Compose for Web    │ │
│  └────┬────┘  └────┬────┘  └─────────┬───────────┘ │
│       │            │                  │             │
│       └────────────┴──────────────────┘             │
│                    │                                 │
│            ┌───────┴────────┐                        │
│            │  shared (KMP)  │                        │
│            │  Domain models │                        │
│            │  DTOs / enums  │                        │
│            └───────┬────────┘                        │
└────────────────────┼────────────────────────────────┘
                     │ HTTP / WebSocket
┌────────────────────┼────────────────────────────────┐
│               Backend (Ktor)                         │
│  ┌─────────────────┴───────────────────────────┐    │
│  │              API Layer (REST)                │    │
│  ├─────────────────────────────────────────────┤    │
│  │           Service Layer                      │    │
│  ├─────────────────────────────────────────────┤    │
│  │         Repository / Persistence             │    │
│  ├─────────────────────────────────────────────┤    │
│  │     SQLite (dev) + File Storage (videos)     │    │
│  └─────────────────────────────────────────────┘    │
│                                                      │
│  ┌──────────────┐  ┌──────────────────────────┐     │
│  │  WebSocket   │  │  Video Processing        │     │
│  │  (Chat/RT)   │  │  (transcode, thumbnail)  │     │
│  └──────────────┘  └──────────────────────────┘     │
└──────────────────────────────────────────────────────┘
```

---

## Module Structure

### `shared/`
Kotlin Multiplatform module shared across all targets.

**Contains:**
- Domain models (entities, enums, value objects)
- DTOs for API request/response contracts
- Shared validation logic
- Constants

**Targets:** Android, iOS, JVM (backend), JS/WASM (web)

### `composeApp/`
Compose Multiplatform UI application.

**Source sets:**
- `commonMain` — Shared Compose UI, navigation, view models
- `androidMain` — Android-specific setup (Activity, manifest)
- `iosMain` — iOS entry point (MainViewController)
- `webMain` — Web entry point

### `server/`
Ktor-based backend application running on JVM.

**Contains:**
- HTTP route definitions (REST API)
- WebSocket handlers (realtime chat, notifications)
- Service layer (business logic)
- Repository layer (database access)
- Authentication/authorization middleware
- Video upload and processing pipeline
- Moderation workflow logic
- Creator licensing logic

### `iosApp/`
Native iOS shell (Swift) that hosts the KMP shared module and Compose UI via interop.

---

## Technology Choices

| Concern | Choice | Rationale |
|---------|--------|-----------|
| Language | Kotlin | KMP-first direction, shared across all targets |
| UI (Android) | Jetpack Compose | Native Android UI toolkit |
| UI (iOS) | Kotlin→Swift interop + Compose | KMP shared modules with native shell |
| UI (Web) | Compose for Web | Secondary client, full-featured |
| Backend framework | Ktor | Kotlin-native, lightweight, KMP-compatible |
| Database (dev) | SQLite | Simple, file-based, specified in requirements |
| Serialization | kotlinx.serialization | KMP-compatible, type-safe |
| HTTP client | Ktor Client | KMP-compatible, shared across platforms |
| DI | Koin | Lightweight, KMP-compatible |
| DB access | Exposed (Kotlin SQL) | Kotlin-native, type-safe SQL DSL, SQLite-compatible |
| Realtime | Ktor WebSockets | Chat and notification delivery |
| AI (onboarding) | Anthropic API (Claude Sonnet) | Category recommendations during onboarding |
| Video storage | Local file system (dev) | On-machine storage during MacBook dev phase |
| Video processing | FFmpeg (via process) | Transcoding and thumbnail generation |

---

## Backend Architecture

### Layered Structure

```
Routes (API endpoints)
    │
    ▼
Services (business logic, orchestration)
    │
    ▼
Repositories (data access, queries)
    │
    ▼
Database (SQLite via Exposed)
```

### Key Backend Services

| Service | Responsibility |
|---------|---------------|
| AuthService | Registration, login, token management, OAuth |
| UserService | Profile management, role queries |
| OnboardingService | Category recommendations (via Anthropic Claude Sonnet), preference storage |
| ContentService | Video/quiz CRUD, status transitions |
| VideoProcessingService | Transcode, thumbnail generation, storage |
| QuizService | Quiz logic, answer validation, scoring |
| FeedService | For You and Deep Dive feed generation |
| SocialService | Follow, friend requests, friendship management |
| ChatService | Conversations, messages, realtime delivery |
| ModerationService | Review queues, decisions, strikes, reports |
| LicensingService | License requests, approval, revocation |
| GamificationService | Points, levels, badges, leaderboards |
| NotificationService | Create and deliver notifications |
| SearchService | Search across videos, quizzes, creators, categories |
| AnalyticsService | Basic creator metrics aggregation |

### Authentication & Authorization

- Token-based auth (JWT)
- Role-based access control on API endpoints
- Middleware checks role permissions before handler execution

### Realtime

- WebSocket connections for authenticated users
- Used for: chat message delivery, notification push
- Simple connection management (no complex pub/sub for MVP)

---

## Client Architecture

### Pattern: MVVM with Unidirectional Data Flow

```
UI (Compose)  ←  ViewModel  ←  Repository  ←  API Client
     │                │
     └── User events ─┘
```

- **ViewModels** hold UI state and handle user actions
- **Repositories** abstract data sources (remote API, local cache)
- **API Client** uses Ktor HTTP client (shared module)
- State is represented with sealed classes/interfaces for explicit states (Loading, Success, Error)

### Navigation

- Compose Navigation (multiplatform) for screen routing
- Bottom navigation or tab bar for main sections
- Top transparent bar for feed switching (For You / Deep Dive)

### Offline Strategy (MVP)

- MVP is online-first; no offline-first sync required
- Basic caching for feed content where practical
- Chat messages are not persisted locally in MVP

---

## Video Pipeline

### Upload Flow
1. Creator selects video file
2. Client uploads to backend via multipart HTTP
3. Backend stores raw file
4. Backend triggers processing (async)
5. Processing generates transcoded video + thumbnail
6. Backend updates video record with processed URLs
7. Video becomes available for playback

### Playback
- Videos served directly from backend (dev) or CDN (production)
- Streaming via HTTP range requests
- ExoPlayer (Android), AVPlayer (iOS), HTML5 video (web)

---

## Hosting Plan

### Phase 1: Development (MacBook)

- All services run locally on the development MacBook
- SQLite database file stored locally
- Video files stored on local filesystem
- Backend accessible via localhost (or local network for device testing)

### Phase 2: Production (Mac Mini)

- Migrate all services to Mac Mini
- Same architecture, different host
- Reconfigure: network addresses, storage paths, environment variables
- See `12-deployment-dev-macbook.md` and `13-deployment-macmini.md` for details

---

## Security Considerations (MVP)

- Passwords hashed with bcrypt
- JWT tokens with expiration
- Role checks on every protected endpoint
- Input validation at API boundaries
- No direct file path exposure (videos served through API)
- CORS configuration for web client
