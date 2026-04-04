# Decision Log

Each entry records an architectural or product decision, its rationale, and consequences.

---

## D001 — SQLite for Development Database

**Date:** 2026-04-04
**Decision:** Use SQLite as the development database.
**Reason:** Specified in the project requirements. Simple, file-based, zero-config. Sufficient for single-developer MVP development.
**Consequences:** No concurrent write support. May need to migrate to PostgreSQL for production scale. All queries must be SQLite-compatible.

## D002 — Ktor for Backend Framework

**Date:** 2026-04-04
**Decision:** Use Ktor as the backend HTTP framework.
**Reason:** Kotlin-native, lightweight, supports WebSockets natively, compatible with KMP shared modules.
**Consequences:** Less opinionated than Spring Boot — requires manual setup for some concerns (DI, auth). Smaller ecosystem but sufficient for MVP.

## D003 — Koin for Dependency Injection

**Date:** 2026-04-04
**Decision:** Use Koin for DI across all modules.
**Reason:** Lightweight, KMP-compatible, simple setup. No code generation required.
**Consequences:** Runtime DI (no compile-time safety). Acceptable for MVP; revisit if DI graph grows complex.

## D004 — Monolithic Backend (Single Ktor Process)

**Date:** 2026-04-04
**Decision:** Run all backend services (HTTP, WebSocket, video processing) in a single Ktor process.
**Reason:** MVP simplicity. No need for microservices at this scale. Deployed on a single machine.
**Consequences:** All services share one process — a crash affects everything. Acceptable for MVP. Video processing could be extracted later if it blocks the event loop.

## D005 — JWT for Authentication

**Date:** 2026-04-04
**Decision:** Use JWT tokens for stateless authentication.
**Reason:** Standard approach, works well with mobile and web clients, no server-side session storage needed.
**Consequences:** Token revocation requires a refresh-token rotation strategy. Tokens must have reasonable expiration.

## D006 — Cursor-Based Pagination

**Date:** 2026-04-04
**Decision:** Use cursor-based pagination for all list endpoints.
**Reason:** Better performance than offset pagination for large datasets. Consistent behavior when data changes between pages.
**Consequences:** Slightly more complex implementation than offset-based. Client must track cursors.

## D007 — Web-Only Moderation Dashboard

**Date:** 2026-04-04
**Decision:** Build the moderation panel as a web-only interface.
**Reason:** Specified in requirements. Moderation is a staff workflow that benefits from desktop UI. Avoids building moderation into mobile apps.
**Consequences:** Moderators need a web browser. The web Compose target handles this.

## D008 — FFmpeg for Video Processing

**Date:** 2026-04-04
**Decision:** Use FFmpeg (via process execution) for video transcoding and thumbnail generation.
**Reason:** Industry standard, available on macOS, handles all common video formats.
**Consequences:** Requires FFmpeg installed on the host machine. Process execution adds complexity. Async processing needed to avoid blocking.

---

## D009 — Exposed for Database Access

**Date:** 2026-04-04
**Decision:** Use JetBrains Exposed (Kotlin SQL framework) for database access.
**Reason:** Approved by product owner. Kotlin-native, type-safe DSL, works well with SQLite and other databases.
**Consequences:** Adds a dependency. Provides both DSL and DAO patterns. Good migration path if database changes later.

## D010 — Anthropic API (Claude Sonnet) for Onboarding Recommendations

**Date:** 2026-04-04
**Decision:** Use the Anthropic API with Claude Sonnet model for AI-powered category recommendations during onboarding.
**Reason:** Approved by product owner. Provides intelligent category suggestions based on user self-description and preference answers.
**Consequences:** Requires an Anthropic API key. Adds external API dependency. Need to handle API failures gracefully (fallback to keyword matching or manual selection).

## D011 — Points and Levels Confirmed

**Date:** 2026-04-04
**Decision:** Points per correct answer: 10 (default/easy), 20 (medium), 30 (hard). Level up every 100 points.
**Reason:** Confirmed by product owner.
**Consequences:** Simple, predictable progression. Easy to adjust later if needed.

## D012 — Strike Consequences: 1-Week Cooldown + License Revocation

**Date:** 2026-04-04
**Decision:** Each strike imposes a 1-week cooldown (no uploads/publishing) and revokes licensed creator status if applicable. Creator must restart the licensing process after cooldown.
**Reason:** Confirmed by product owner. Strikes are punitive but recoverable.
**Consequences:** Need to track cooldown_until per strike. Need to check cooldown before allowing uploads. License revocation on strike is independent of the 3-rejection rule.

## D013 — No Video Limits in Development

**Date:** 2026-04-04
**Decision:** No file size, duration, or resolution limits on video uploads during development.
**Reason:** Development environment; limits will be added for production.
**Consequences:** Local storage may grow large. Acceptable for dev.

## D014 — Search in MVP

**Date:** 2026-04-04
**Decision:** Include TikTok-style search functionality in MVP. Search across videos, quizzes, creators, and categories.
**Reason:** Confirmed by product owner as essential for content discovery.
**Consequences:** Adds search API, search UI, and recent search history. Uses SQL LIKE queries for MVP (no full-text search engine).

---

*Add new decisions below this line.*
