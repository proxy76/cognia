# API Contracts

All endpoints use JSON. Authentication is via `Authorization: Bearer <token>` header unless marked as public.

Base path: `/api/v1`

---

## Authentication

### POST `/auth/register` (public)

Register a new user with email/password.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123",
  "displayName": "Jane Doe"
}
```

**Response (201):**
```json
{
  "userId": "uuid",
  "token": "jwt-token",
  "refreshToken": "refresh-token"
}
```

### POST `/auth/login` (public)

**Request:**
```json
{
  "email": "user@example.com",
  "password": "securePassword123"
}
```

**Response (200):**
```json
{
  "userId": "uuid",
  "token": "jwt-token",
  "refreshToken": "refresh-token"
}
```

### POST `/auth/oauth/google` (public)

**Request:**
```json
{
  "idToken": "google-id-token"
}
```

**Response (200):** Same as login.

### POST `/auth/oauth/apple` (public)

**Request:**
```json
{
  "identityToken": "apple-identity-token",
  "authorizationCode": "apple-auth-code"
}
```

**Response (200):** Same as login.

### POST `/auth/refresh` (public)

**Request:**
```json
{
  "refreshToken": "refresh-token"
}
```

**Response (200):**
```json
{
  "token": "new-jwt-token",
  "refreshToken": "new-refresh-token"
}
```

---

## Onboarding

### POST `/onboarding/recommend`

AI-powered category recommendations.

**Request:**
```json
{
  "selfDescription": "I'm a high school student interested in science and history",
  "answers": [
    { "questionId": "q1", "answer": "I like visual learning" }
  ]
}
```

**Response (200):**
```json
{
  "recommendedCategories": [
    { "id": "uuid", "name": "Physics", "slug": "physics" },
    { "id": "uuid", "name": "World History", "slug": "world-history" }
  ]
}
```

### PUT `/onboarding/preferences`

Save user's selected categories.

**Request:**
```json
{
  "categoryIds": ["uuid1", "uuid2", "uuid3"],
  "selfDescription": "I'm a high school student interested in science and history"
}
```

**Response (200):**
```json
{
  "status": "saved"
}
```

### GET `/categories`

List all available categories.

**Response (200):**
```json
{
  "categories": [
    { "id": "uuid", "name": "Physics", "slug": "physics" }
  ]
}
```

---

## User / Profile

### GET `/users/me`

Get current user's profile.

**Response (200):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "displayName": "Jane Doe",
  "avatarUrl": "https://...",
  "role": "LEARNER",
  "level": 5,
  "totalPoints": 1250,
  "categories": [
    { "id": "uuid", "name": "Physics" }
  ],
  "badgeCount": 3,
  "followerCount": 0,
  "followingCount": 5,
  "friendCount": 12
}
```

### PUT `/users/me`

Update profile.

**Request:**
```json
{
  "displayName": "Jane D.",
  "avatarUrl": "https://..."
}
```

### GET `/users/{userId}`

Get another user's public profile.

**Response (200):**
```json
{
  "id": "uuid",
  "displayName": "Expert Creator",
  "avatarUrl": "https://...",
  "role": "LICENSED_CREATOR",
  "level": 12,
  "followerCount": 340,
  "videoCount": 25,
  "badges": [
    { "id": "uuid", "name": "First Quiz", "iconUrl": "https://..." }
  ]
}
```

---

## Content — Videos

### POST `/videos` (creator)

Upload a new video. Multipart form data.

**Form fields:**
- `file` — Video file (binary)
- `title` — String
- `description` — String (optional)
- `categoryId` — UUID

**Response (201):**
```json
{
  "id": "uuid",
  "status": "DRAFT",
  "title": "Quantum Mechanics Basics",
  "createdAt": "2026-01-15T10:30:00Z"
}
```

### PUT `/videos/{videoId}` (creator, owner)

Update video metadata.

**Request:**
```json
{
  "title": "Updated Title",
  "description": "Updated description",
  "categoryId": "uuid",
  "difficulty": "MEDIUM"
}
```

Note: `difficulty` can only be set by licensed creators.

### POST `/videos/{videoId}/submit` (regular creator, owner)

Submit video for moderation review.

**Response (200):**
```json
{
  "id": "uuid",
  "status": "PENDING_REVIEW"
}
```

### POST `/videos/{videoId}/publish` (licensed creator, owner)

Publish video instantly.

**Response (200):**
```json
{
  "id": "uuid",
  "status": "PUBLISHED"
}
```

### GET `/videos/{videoId}` (public, if published)

**Response (200):**
```json
{
  "id": "uuid",
  "creator": { "id": "uuid", "displayName": "Expert" },
  "title": "Quantum Mechanics Basics",
  "description": "...",
  "category": { "id": "uuid", "name": "Physics" },
  "videoUrl": "https://...",
  "thumbnailUrl": "https://...",
  "difficulty": "MEDIUM",
  "publishedAt": "2026-01-15T12:00:00Z",
  "viewCount": 1500,
  "quizId": "uuid or null"
}
```

### DELETE `/videos/{videoId}` (creator owner or moderator)

Soft delete / remove video.

---

## Content — Quizzes

### POST `/quizzes` (creator)

Create a new quiz.

**Request:**
```json
{
  "title": "Physics Quiz 1",
  "quizType": "MULTIPLE_CHOICE",
  "categoryId": "uuid",
  "videoId": "uuid or null",
  "difficulty": "HARD",
  "questions": [
    {
      "questionText": "What is the speed of light?",
      "options": [
        { "text": "3x10^8 m/s" },
        { "text": "3x10^6 m/s" },
        { "text": "3x10^10 m/s" },
        { "text": "3x10^4 m/s" }
      ],
      "correctOptionIndex": 0
    }
  ]
}
```

**Response (201):**
```json
{
  "id": "uuid",
  "status": "DRAFT"
}
```

### POST `/quizzes/{quizId}/submit` (regular creator)

Submit quiz for review.

### POST `/quizzes/{quizId}/publish` (licensed creator)

Publish quiz instantly.

### GET `/quizzes/{quizId}`

Get quiz for taking (correct answers not included).

**Response (200):**
```json
{
  "id": "uuid",
  "title": "Physics Quiz 1",
  "quizType": "MULTIPLE_CHOICE",
  "difficulty": "HARD",
  "questions": [
    {
      "id": "uuid",
      "questionText": "What is the speed of light?",
      "options": [
        { "id": "uuid", "text": "3x10^8 m/s" },
        { "id": "uuid", "text": "3x10^6 m/s" }
      ]
    }
  ]
}
```

### POST `/quizzes/{quizId}/attempt`

Submit quiz answers.

**Request:**
```json
{
  "answers": [
    { "questionId": "uuid", "selectedOptionIndex": 0 }
  ]
}
```

**Response (200):**
```json
{
  "score": 4,
  "totalQuestions": 5,
  "pointsAwarded": 120,
  "results": [
    { "questionId": "uuid", "correct": true, "correctOptionIndex": 0 }
  ]
}
```

---

## Feeds

### GET `/feeds/for-you?cursor={cursor}&limit={limit}`

**Response (200):**
```json
{
  "items": [
    {
      "type": "VIDEO",
      "video": { "id": "uuid", "title": "...", "thumbnailUrl": "...", "creator": {...}, "quizId": "uuid or null" }
    }
  ],
  "nextCursor": "cursor-string"
}
```

### GET `/feeds/deep-dive?cursor={cursor}&limit={limit}`

Same response shape as For You feed.

---

## Search

### GET `/search?q={query}&type={type}&cursor={cursor}&limit={limit}`

Search across content, creators, and categories. TikTok-style unified search.

**Query parameters:**
- `q` — Search query string (required)
- `type` — Optional filter: `all` (default), `videos`, `quizzes`, `creators`, `categories`
- `cursor` — Pagination cursor
- `limit` — Results per page (default 20)

**Response (200):**
```json
{
  "query": "physics",
  "results": {
    "videos": [
      {
        "id": "uuid",
        "title": "Quantum Mechanics Basics",
        "creator": { "id": "uuid", "displayName": "Expert" },
        "thumbnailUrl": "https://...",
        "category": { "id": "uuid", "name": "Physics" }
      }
    ],
    "quizzes": [
      {
        "id": "uuid",
        "title": "Physics Quiz 1",
        "quizType": "MULTIPLE_CHOICE",
        "category": { "id": "uuid", "name": "Physics" }
      }
    ],
    "creators": [
      {
        "id": "uuid",
        "displayName": "Physics Prof",
        "avatarUrl": "https://...",
        "followerCount": 500
      }
    ],
    "categories": [
      { "id": "uuid", "name": "Physics", "slug": "physics" }
    ]
  },
  "nextCursor": "cursor-string"
}
```

When `type` is specified (not `all`), only that section is populated with full pagination.

### GET `/search/recent`

Get user's recent search queries.

**Response (200):**
```json
{
  "recentSearches": ["physics", "world history", "math"]
}
```

### DELETE `/search/recent`

Clear recent search history.

---

## Social — Follow

### POST `/users/{userId}/follow`

Follow a creator.

### DELETE `/users/{userId}/follow`

Unfollow a creator.

### GET `/users/{userId}/followers?cursor={cursor}&limit={limit}`

### GET `/users/{userId}/following?cursor={cursor}&limit={limit}`

---

## Social — Friends

### POST `/friends/request`

**Request:**
```json
{
  "userId": "target-user-uuid"
}
```

### PUT `/friends/request/{requestId}`

Accept or decline.

**Request:**
```json
{
  "action": "ACCEPT"
}
```

Action values: `ACCEPT`, `DECLINE`

### GET `/friends?cursor={cursor}&limit={limit}`

List current user's friends.

### DELETE `/friends/{friendshipId}`

Remove a friend.

---

## Social — Chat

### GET `/chat/conversations?cursor={cursor}&limit={limit}`

List conversations.

**Response (200):**
```json
{
  "conversations": [
    {
      "id": "uuid",
      "participant": { "id": "uuid", "displayName": "...", "avatarUrl": "..." },
      "lastMessage": { "text": "Hey!", "createdAt": "..." },
      "unreadCount": 2
    }
  ]
}
```

### GET `/chat/conversations/{conversationId}/messages?cursor={cursor}&limit={limit}`

### POST `/chat/conversations/{conversationId}/messages`

**Request (text):**
```json
{
  "messageType": "TEXT",
  "textContent": "Hello!"
}
```

**Request (shared post):**
```json
{
  "messageType": "SHARED_POST",
  "sharedContentId": "uuid",
  "sharedContentType": "VIDEO"
}
```

### WebSocket `/chat/ws`

Realtime message delivery. After auth handshake, server pushes new messages as JSON frames.

---

## Moderation

### GET `/moderation/queue?type={VIDEO|QUIZ}&status={PENDING}&cursor={cursor}` (moderator)

**Response (200):**
```json
{
  "items": [
    {
      "reviewId": "uuid",
      "contentType": "VIDEO",
      "contentId": "uuid",
      "title": "...",
      "creator": { "id": "uuid", "displayName": "..." },
      "isPostPublication": false,
      "submittedAt": "..."
    }
  ]
}
```

### POST `/moderation/reviews/{reviewId}/decide` (moderator)

**Request:**
```json
{
  "decision": "APPROVED",
  "reason": "Content is accurate and safe"
}
```

### GET `/moderation/reports?status={PENDING}&cursor={cursor}` (moderator)

### POST `/moderation/reports/{reportId}/review` (moderator)

**Request:**
```json
{
  "action": "DISMISS",
  "reason": "Content is within guidelines"
}
```

Action values: `STRIKE`, `REMOVE_CONTENT`, `DISMISS`

### POST `/moderation/strikes` (moderator)

**Request:**
```json
{
  "userId": "uuid",
  "reason": "Repeated misleading content"
}
```

---

## Creator Licensing

### POST `/licensing/request` (regular creator with 5+ approved videos)

Request licensed creator status.

### GET `/licensing/requests?status={PENDING}` (moderator)

### POST `/licensing/requests/{requestId}/decide` (moderator)

**Request:**
```json
{
  "decision": "APPROVED"
}
```

Decision values: `APPROVED`, `REJECTED` (with optional `reason`)

---

## Creator Analytics

### GET `/analytics/me` (creator)

**Response (200):**
```json
{
  "totalViews": 15000,
  "totalQuizAttempts": 3200,
  "averageCorrectRate": 0.72,
  "totalShares": 450,
  "videoStats": [
    {
      "videoId": "uuid",
      "title": "...",
      "views": 5000,
      "quizAttempts": 1200,
      "correctRate": 0.68,
      "shares": 150
    }
  ]
}
```

---

## Gamification

### GET `/leaderboard/{type}?limit={limit}`

Type: `friends` or `global`

**Response (200):**
```json
{
  "entries": [
    {
      "rank": 1,
      "user": { "id": "uuid", "displayName": "...", "avatarUrl": "..." },
      "totalPoints": 5000,
      "level": 15
    }
  ]
}
```

### GET `/badges`

List all available badges.

### GET `/users/me/badges`

List badges earned by current user.

---

## Notifications

### GET `/notifications?cursor={cursor}&limit={limit}`

**Response (200):**
```json
{
  "notifications": [
    {
      "id": "uuid",
      "type": "CONTENT_APPROVED",
      "title": "Video approved",
      "body": "Your video 'Quantum Mechanics Basics' has been approved",
      "referenceId": "uuid",
      "referenceType": "VIDEO",
      "read": false,
      "createdAt": "..."
    }
  ],
  "unreadCount": 5
}
```

### PUT `/notifications/{notificationId}/read`

Mark notification as read.

### PUT `/notifications/read-all`

Mark all notifications as read.

### WebSocket `/notifications/ws`

Realtime notification delivery.

---

## Common Response Patterns

### Error Response
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Email is already registered"
  }
}
```

### Pagination
All list endpoints use cursor-based pagination with `cursor` and `limit` query parameters.

### HTTP Status Codes
- 200 — Success
- 201 — Created
- 400 — Bad request / validation error
- 401 — Unauthorized
- 403 — Forbidden (insufficient role)
- 404 — Not found
- 409 — Conflict (e.g., duplicate)
- 500 — Internal server error
