# Domain Model

This document defines the core entities, enums, and relationships for the Cognia MVP.

---

## Core Entities

### User

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| email | String | Unique |
| displayName | String | |
| avatarUrl | String? | Optional |
| role | UserRole | Current active role |
| createdAt | Instant | |
| updatedAt | Instant | |

### UserProfile

| Field | Type | Notes |
|-------|------|-------|
| userId | UUID | FK → User |
| selfDescription | String? | Free-text from onboarding |
| categories | List\<CategoryId\> | Selected during onboarding |
| level | Int | Derived from points |
| totalPoints | Long | Accumulated quiz points |

### Category

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| name | String | e.g. "Physics", "History" |
| slug | String | URL-safe identifier |

---

## Content Entities

### Video (Reel)

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| creatorId | UUID | FK → User |
| title | String | |
| description | String? | |
| categoryId | UUID | FK → Category |
| videoUrl | String | Storage URL |
| thumbnailUrl | String? | Generated during processing |
| status | ContentStatus | Current lifecycle state |
| difficulty | Difficulty? | Only set by licensed creators |
| createdAt | Instant | |
| publishedAt | Instant? | Set when published |

### Quiz

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| creatorId | UUID | FK → User |
| videoId | UUID? | FK → Video; null if standalone |
| title | String | |
| quizType | QuizType | |
| categoryId | UUID | FK → Category |
| status | ContentStatus | Same lifecycle as video |
| difficulty | Difficulty? | Only set by licensed creators |
| createdAt | Instant | |

### QuizQuestion

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| quizId | UUID | FK → Quiz |
| questionText | String | |
| options | List\<QuizOption\> | For multiple choice |
| correctOptionIndex | Int | Index of correct answer |
| orderIndex | Int | Question ordering |

### QuizOption

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| questionId | UUID | FK → QuizQuestion |
| text | String | |
| orderIndex | Int | Option ordering |

### QuizAttempt

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| userId | UUID | FK → User |
| quizId | UUID | FK → Quiz |
| score | Int | Number of correct answers |
| totalQuestions | Int | |
| pointsAwarded | Long | Based on difficulty |
| completedAt | Instant | |

---

## Social Entities

### Follow

| Field | Type | Notes |
|-------|------|-------|
| followerId | UUID | FK → User (the follower) |
| followedId | UUID | FK → User (the creator being followed) |
| createdAt | Instant | |

Constraint: One-way. Used for following creators.

### Friendship

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| requesterId | UUID | FK → User |
| receiverId | UUID | FK → User |
| status | FriendshipStatus | |
| createdAt | Instant | |
| acceptedAt | Instant? | |

### ChatConversation

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| participantA | UUID | FK → User |
| participantB | UUID | FK → User |
| createdAt | Instant | |
| lastMessageAt | Instant? | |

### ChatMessage

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| conversationId | UUID | FK → ChatConversation |
| senderId | UUID | FK → User |
| messageType | ChatMessageType | |
| textContent | String? | For text messages |
| sharedContentId | UUID? | FK → Video or Quiz (for shared posts) |
| sharedContentType | SharedContentType? | VIDEO or QUIZ |
| createdAt | Instant | |

---

## Moderation Entities

### ModerationReview

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| contentId | UUID | FK → Video or Quiz |
| contentType | ContentEntityType | VIDEO or QUIZ |
| moderatorId | UUID? | FK → User; null while in queue |
| decision | ModerationDecision? | null while pending |
| reason | String? | Moderator's note |
| isPostPublication | Boolean | true for licensed creator reviews |
| createdAt | Instant | |
| decidedAt | Instant? | |

### ContentReport

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| reporterId | UUID | FK → User |
| contentId | UUID | FK → Video or Quiz |
| contentType | ContentEntityType | VIDEO or QUIZ |
| reason | ReportReason | |
| status | ReportStatus | |
| createdAt | Instant | |

### Strike

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| userId | UUID | FK → User (creator) |
| moderatorId | UUID | FK → User (moderator who issued) |
| reason | String | |
| cooldownUntil | Instant | createdAt + 7 days |
| createdAt | Instant | |

### CreatorLicenseRequest

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| creatorId | UUID | FK → User |
| status | LicenseRequestStatus | |
| moderatorId | UUID? | FK → User; null while pending |
| rejectionReason | String? | |
| createdAt | Instant | |
| decidedAt | Instant? | |

---

## Gamification Entities

### Badge

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| name | String | |
| description | String | |
| iconUrl | String | |
| criteria | String | Human-readable unlock condition |

### UserBadge

| Field | Type | Notes |
|-------|------|-------|
| userId | UUID | FK → User |
| badgeId | UUID | FK → Badge |
| awardedAt | Instant | |

### LeaderboardEntry

Computed/materialized, not necessarily a persistent entity. Represents a user's rank.

| Field | Type | Notes |
|-------|------|-------|
| userId | UUID | FK → User |
| leaderboardType | LeaderboardType | FRIENDS or GLOBAL |
| totalPoints | Long | |
| rank | Int | Computed |

---

## Notification Entity

### Notification

| Field | Type | Notes |
|-------|------|-------|
| id | UUID | Primary key |
| userId | UUID | FK → User (recipient) |
| type | NotificationType | |
| title | String | |
| body | String | |
| referenceId | UUID? | FK to related entity |
| referenceType | String? | Entity type for deep linking |
| read | Boolean | Default false |
| createdAt | Instant | |

---

## Enums

### UserRole
```
LEARNER
REGULAR_CREATOR
LICENSED_CREATOR
MODERATOR
ADMIN
```

### ContentStatus
```
DRAFT                  -- Creator is still editing
PENDING_REVIEW         -- Submitted by regular creator, awaiting moderation
APPROVED               -- Passed moderation, ready to publish
REJECTED               -- Failed moderation
PUBLISHED              -- Live and visible to users
PENDING_POST_REVIEW    -- Published by licensed creator, awaiting post-pub review
DELETED                -- Removed (e.g., licensed content rejected post-pub)
```

### QuizType
```
MULTIPLE_CHOICE
TRUE_FALSE
```

### Difficulty
```
EASY
MEDIUM
HARD
```

### FriendshipStatus
```
PENDING
ACCEPTED
DECLINED
```

### ChatMessageType
```
TEXT
SHARED_POST
```

### SharedContentType
```
VIDEO
QUIZ
```

### ContentEntityType
```
VIDEO
QUIZ
```

### ModerationDecision
```
APPROVED
REJECTED
```

### ReportReason
```
BAD_CONTENT
MISLEADING_CONTENT
```

### ReportStatus
```
PENDING
REVIEWED
DISMISSED
```

### LicenseRequestStatus
```
PENDING
APPROVED
REJECTED
```

### LeaderboardType
```
FRIENDS
GLOBAL
```

### NotificationType
```
FRIEND_REQUEST_RECEIVED
FRIEND_REQUEST_ACCEPTED
CONTENT_APPROVED
CONTENT_REJECTED
BADGE_EARNED
LEVEL_UP
NEW_FOLLOWER
STRIKE_ISSUED
LICENSE_APPROVED
LICENSE_REJECTED
LICENSE_REVOKED
```

> Note: CHAT_MESSAGE notifications are deferred — chat has its own realtime delivery via WebSocket.

---

## Points and Difficulty Mapping

| Difficulty | Points per Correct Answer |
|------------|--------------------------|
| None (default) | 10 |
| EASY | 10 |
| MEDIUM | 20 |
| HARD | 30 |

## Level Thresholds

Level up every **100 points**. Level = floor(totalPoints / 100) + 1.

| Level | Points Required |
|-------|----------------|
| 1 | 0 |
| 2 | 100 |
| 3 | 200 |
| N | (N-1) * 100 |

## Badges (MVP Set)

| Badge | Criteria |
|-------|----------|
| First Quiz | Complete your first quiz |
| Quiz Streak 10 | Complete 10 quizzes |
| First Video | Upload your first video (creator) |
| Perfect Score | Get 100% on a quiz |
| Social Butterfly | Add 5 friends |
| Knowledge Seeker | Engage with 5 different categories |

## Strike Consequences

When a creator receives a strike:
- **1-week cooldown** period: creator cannot upload or publish content
- **Licensed status revoked** (if applicable): must restart the licensing process from scratch after cooldown ends
- At **3 strikes**: permanent restrictions (cooldown + license revocation applied on each strike)
