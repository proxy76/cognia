# MVP Scope

This document defines the exact feature set for the Cognia MVP. Only features listed here are in scope. Anything not listed is explicitly excluded (see `02-non-goals.md`).

---

## Authentication

- Email/password registration and login
- Google OAuth sign-in
- Apple Sign-In
- Underage users are allowed; no parental controls required at launch
- Session management (token-based)

## AI-Assisted Onboarding

- AI asks preference questions during onboarding
- User provides a free-text self-description
- AI recommends categories based on responses
- User can deselect AI suggestions
- User can manually add more categories
- Preferences are editable later from profile settings

## Feeds

- **For You feed**: Content sourced from selected categories, AI recommendations, and user behavior
- **Deep Dive feed**: Content from exploration topics outside usual preferences
- Top transparent switch bar (TikTok-style) to toggle between feeds
- If user repeatedly engages with Deep Dive topics, those topics may later appear in For You

## Search

- TikTok-style search screen accessible from navigation
- Search by keywords across videos, quizzes, creators, and categories
- Search results grouped by type (content, creators, categories)
- Recent searches history

## Content — Reels / Video

- Creator video upload flow with metadata entry
- Video storage integration
- Video processing pipeline (transcode, thumbnail generation)
- Vertical reel playback UI

## Content — Quizzes

- Multiple choice quizzes
- True/false quizzes
- Quizzes can be attached to a video or standalone
- Quiz answer validation and scoring

## Gamification

- Points awarded for correct quiz answers
- Licensed creators can set difficulty (easy, medium, hard) on eligible content
- Difficulty affects point value
- Levels (earned through accumulated points)
- Badges (earned through specific achievements)
- Friends leaderboard
- Global leaderboard

## Social

- Follow creators (one-way)
- Friend system (mutual add)
- Private 1:1 chat
- Chat supports text messages and shared posts (content sharing)
- No images, voice notes, or group chat at launch

## Strike Consequences

- Each strike imposes a **1-week cooldown** (creator cannot upload or publish)
- If the creator was licensed, the **licensed role is revoked** and they must restart the licensing process after cooldown
- 3 strikes is the limit (cooldown + revocation applied on each strike)

## Moderation

- Regular creator content goes through moderator review before publishing
- Licensed creator content publishes instantly but is reviewed afterward
- If licensed content is rejected, it is deleted
- 3 rejected licensed videos revoke the licensed creator role
- Users can report bad or misleading content
- Reports are reviewed by moderators
- Strike system: 3 strikes limit
- Moderation panel is web-only at launch
- Review criteria: accuracy and safety

## Creator Licensing

- Creator can request licensed status after 5 approved videos
- Moderator reviews creator history and approves or rejects the request
- Licensed creators publish instantly (post-publication review)
- 3 rejected licensed videos revoke the licensed role; creator must re-apply later

## Creator Analytics (Basic)

- Views
- Quiz attempts
- Correct answer rate
- Shares
- Basic engagement metrics

## Notifications

- Notification delivery for relevant events (friend requests, chat messages, content approvals, etc.)
- Realtime delivery support

## Roles

- Learner
- Regular Creator
- Licensed Creator
- Moderator
- Admin
