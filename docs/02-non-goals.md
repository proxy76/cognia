# Non-Goals

This document explicitly lists features, capabilities, and behaviors that are **not** part of the Cognia MVP. These items must not be implemented, designed for, or planned around unless explicitly moved into scope by the product owner.

---

## Excluded Content Types

- **Cards** — Deferred to post-MVP (phase 9+)
- **Minigames** — Deferred; only quizzes are in MVP
- **Combined content formats** (reels + minigames, reels + quizzes, cards + quizzes) — Not in MVP
- **Advanced quiz types** beyond multiple choice and true/false — Not in MVP

## Excluded Social Features

- **Group chat** — Not in MVP; only 1:1 private chat
- **Image messages** in chat — Not in MVP
- **Voice notes** in chat — Not in MVP
- **Comments on videos** — Deferred to post-MVP (phase 9)
- **Comment moderation and cooldown** — Deferred with comments

## Excluded Gamification / Engagement

- **Battles** — Deferred to post-MVP (phase 11)
- **Timed challenge system** — Deferred with battles
- **Battle scoring and rewards** — Deferred with battles
- **XP system** — Listed as future expansion, not in MVP
- **Explain Like I Am 5 (ELI5)** — Deferred to post-MVP (phase 10)

## Excluded Moderation Features

- **AI-assisted moderation** — Explicitly set to false in source spec
- **Comment moderation cooldown** — Deferred with comments

## Excluded Analytics / Creator Tools

- **Monetization** — No monetization at launch
- **Advanced retention breakdown** — Not in MVP
- **Draft scheduling** — Not in MVP
- **Version history** for content — Not in MVP
- **Advanced creator dashboards** — Not in MVP

## Excluded Notification Types (Deferred)

- **Chat message notifications** — Chat has its own realtime WebSocket delivery; no separate notification needed at launch

## Excluded Platform Features

- **Parental controls** — Not required despite allowing underage users
- **Localization / i18n** — English only at launch; localization is future work
- **Advanced accessibility layers** — Deferred to post-MVP

## Excluded Architecture / Infrastructure

- **Microservices architecture** — Use simple service boundaries
- **Complex event-driven architectures** — Not required for MVP
- **AI moderation pipeline** — Not in MVP
- **Production database migration** from SQLite — Development uses SQLite; production DB decisions are separate and deferred

---

**Rule:** If any task or implementation touches a non-goal listed above, stop and flag it. Do not silently expand scope.
