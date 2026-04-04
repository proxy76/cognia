# Task Roadmap

Tasks are organized by milestone. Each milestone builds on the previous. Tasks within a milestone are ordered by dependency. Detailed task files are in `docs/tasks/`.

---

## M1 — Foundation

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M1-001 | Define shared domain models (enums, core entities) | — |
| M1-002 | Set up backend skeleton with Ktor | — |
| M1-003 | Set up SQLite database layer with schema migration | M1-002 |
| M1-004 | Set up dependency injection (Koin) | M1-002 |
| M1-005 | Set up shared networking (Ktor Client) | M1-001 |
| M1-006 | Set up Compose navigation shell | M1-005 |
| M1-007 | Define DTO serialization contracts | M1-001 |
| M1-008 | Set up environment configuration (ports, paths, secrets) | M1-002 |

## M2 — Authentication & Profiles

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M2-001 | Implement auth domain models and DTOs | M1-001, M1-007 |
| M2-002 | Implement registration and login API (email/password) | M1-003, M2-001 |
| M2-003 | Implement JWT token issuance and validation | M2-002 |
| M2-004 | Implement OAuth endpoints (Google, Apple) | M2-003 |
| M2-005 | Implement role-based authorization middleware | M2-003 |
| M2-006 | Implement user profile API (get, update) | M2-003 |
| M2-007 | Build registration/login UI screens | M1-006, M2-002 |
| M2-008 | Build profile screen UI | M2-006, M2-007 |

## M3 — Onboarding

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M3-001 | Implement category CRUD and seed data | M1-003 |
| M3-002 | Implement onboarding recommendation API contract | M3-001 |
| M3-003 | Implement preference persistence API | M3-001, M2-003 |
| M3-004 | Build onboarding UI flow (questions, description, category selection) | M1-006, M3-002, M3-003 |

## M4 — Content & Playback

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M4-001 | Implement video entity and content status state machine | M1-001, M1-003 |
| M4-002 | Implement video upload API (multipart) | M4-001, M2-005 |
| M4-003 | Implement video processing pipeline (transcode, thumbnail) | M4-002 |
| M4-004 | Implement video metadata update and status transition APIs | M4-001, M2-005 |
| M4-005 | Implement video serving endpoint (streaming) | M4-003 |
| M4-006 | Build creator upload UI | M1-006, M4-002 |
| M4-007 | Build reel playback UI | M4-005 |

## M5 — Quizzes & Progression

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M5-001 | Implement quiz domain models (quiz, questions, options) | M1-001, M1-003 |
| M5-002 | Implement quiz CRUD API | M5-001, M2-005 |
| M5-003 | Implement quiz attempt and scoring logic | M5-001 |
| M5-004 | Implement difficulty-based points calculation | M5-003 |
| M5-005 | Implement levels system | M5-004 |
| M5-006 | Implement badges system | M5-005 |
| M5-007 | Build quiz-taking UI | M1-006, M5-002 |
| M5-008 | Build quiz creation UI (creator) | M5-002, M4-006 |

## M6 — Feeds

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M6-001 | Implement For You feed generation logic | M4-001, M3-003 |
| M6-002 | Implement Deep Dive feed generation logic | M4-001 |
| M6-003 | Implement feed API endpoints | M6-001, M6-002 |
| M6-004 | Implement feed behavior tracking (engagement events) | M6-003 |
| M6-005 | Build feed UI with switching bar | M1-006, M6-003, M4-007 |
| M6-006 | Search API (keyword search across content, creators, categories) | M4-001, M5-001, M2-005, M3-001 |
| M6-007 | Search UI | M6-006, M1-006 |

## M7 — Social

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M7-001 | Implement follow system (model, API) | M2-005 |
| M7-002 | Implement friend request system (model, API) | M2-005 |
| M7-003 | Implement chat domain (conversations, messages) | M2-005, M1-003 |
| M7-004 | Implement chat REST API | M7-003 |
| M7-005 | Implement chat WebSocket for realtime delivery | M7-003 |
| M7-006 | Implement shared post message type in chat | M7-003, M4-001 |
| M7-007 | Build follow/unfollow UI | M7-001, M2-008 |
| M7-008 | Build friend request UI | M7-002 |
| M7-009 | Build chat UI | M7-004, M7-005 |

## M8 — Moderation & Licensing

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M8-001 | Implement moderation review queue (model, API) | M4-001, M2-005 |
| M8-002 | Implement moderation decision API (approve/reject) | M8-001 |
| M8-003 | Implement content report system | M8-001 |
| M8-004 | Implement strike logic | M8-002 |
| M8-005 | Implement creator license request API | M2-005 |
| M8-006 | Implement license approval and revocation logic | M8-005, M8-002 |
| M8-007 | Build web moderation dashboard | M8-001, M8-002, M8-003, M8-005 |

## M9 — Notifications, Analytics & Polish

| Task ID | Title | Dependencies |
|---------|-------|-------------|
| M9-001 | Implement notification creation and storage | M1-003 |
| M9-002 | Implement notification REST API | M9-001 |
| M9-003 | Implement notification WebSocket delivery | M9-001 |
| M9-004 | Implement basic creator analytics API | M4-001, M5-001 |
| M9-005 | Implement leaderboards API (friends, global) | M5-005, M7-002 |
| M9-006 | Build notification UI | M9-002, M9-003 |
| M9-007 | Build creator analytics UI | M9-004 |
| M9-008 | Build leaderboard UI | M9-005 |
| M9-009 | End-to-end flow testing and bug fixes | All above |

---

## Summary

| Milestone | Task Count | Focus |
|-----------|-----------|-------|
| M1 | 8 | Foundation, shared modules, infrastructure |
| M2 | 8 | Auth, profiles, roles |
| M3 | 4 | AI onboarding, categories |
| M4 | 7 | Video upload, processing, playback |
| M5 | 8 | Quizzes, scoring, levels, badges |
| M6 | 7 | Feed generation, switching, search |
| M7 | 9 | Social graph, chat, realtime |
| M8 | 7 | Moderation, licensing, web dashboard |
| M9 | 9 | Notifications, analytics, leaderboards, polish |
| **Total** | **67** | |
