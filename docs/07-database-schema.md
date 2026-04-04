# Database Schema

Development database: **SQLite**. Schema uses SQLite-compatible types. All UUIDs stored as TEXT. Timestamps stored as TEXT (ISO-8601).

---

## Users & Profiles

```sql
CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT, -- null for OAuth-only users
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    role TEXT NOT NULL DEFAULT 'LEARNER', -- UserRole enum
    auth_provider TEXT NOT NULL DEFAULT 'EMAIL', -- EMAIL, GOOGLE, APPLE
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE user_profiles (
    user_id TEXT PRIMARY KEY REFERENCES users(id),
    self_description TEXT,
    level INTEGER NOT NULL DEFAULT 1,
    total_points INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE user_categories (
    user_id TEXT NOT NULL REFERENCES users(id),
    category_id TEXT NOT NULL REFERENCES categories(id),
    PRIMARY KEY (user_id, category_id)
);

CREATE TABLE refresh_tokens (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    token_hash TEXT NOT NULL UNIQUE,
    expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL
);
```

## Categories

```sql
CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE
);
```

## Content — Videos

```sql
CREATE TABLE videos (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    title TEXT NOT NULL,
    description TEXT,
    category_id TEXT NOT NULL REFERENCES categories(id),
    video_url TEXT, -- set after processing
    thumbnail_url TEXT, -- set after processing
    raw_file_path TEXT, -- local path to uploaded raw file
    status TEXT NOT NULL DEFAULT 'DRAFT', -- ContentStatus enum
    difficulty TEXT, -- Difficulty enum; null unless set by licensed creator
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    published_at TEXT
);

CREATE INDEX idx_videos_creator ON videos(creator_id);
CREATE INDEX idx_videos_status ON videos(status);
CREATE INDEX idx_videos_category ON videos(category_id);
CREATE INDEX idx_videos_published ON videos(published_at);
```

## Content — Quizzes

```sql
CREATE TABLE quizzes (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    video_id TEXT REFERENCES videos(id), -- null if standalone
    title TEXT NOT NULL,
    quiz_type TEXT NOT NULL, -- QuizType enum
    category_id TEXT NOT NULL REFERENCES categories(id),
    status TEXT NOT NULL DEFAULT 'DRAFT', -- ContentStatus enum
    difficulty TEXT, -- Difficulty enum
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE quiz_questions (
    id TEXT PRIMARY KEY,
    quiz_id TEXT NOT NULL REFERENCES quizzes(id),
    question_text TEXT NOT NULL,
    correct_option_index INTEGER NOT NULL,
    order_index INTEGER NOT NULL
);

CREATE TABLE quiz_options (
    id TEXT PRIMARY KEY,
    question_id TEXT NOT NULL REFERENCES quiz_questions(id),
    text TEXT NOT NULL,
    order_index INTEGER NOT NULL
);

CREATE TABLE quiz_attempts (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    quiz_id TEXT NOT NULL REFERENCES quizzes(id),
    score INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    points_awarded INTEGER NOT NULL,
    completed_at TEXT NOT NULL
);

CREATE INDEX idx_quiz_attempts_user ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_quiz ON quiz_attempts(quiz_id);
```

## Social — Follows

```sql
CREATE TABLE follows (
    follower_id TEXT NOT NULL REFERENCES users(id),
    followed_id TEXT NOT NULL REFERENCES users(id),
    created_at TEXT NOT NULL,
    PRIMARY KEY (follower_id, followed_id)
);

CREATE INDEX idx_follows_followed ON follows(followed_id);
```

## Social — Friendships

```sql
CREATE TABLE friendships (
    id TEXT PRIMARY KEY,
    requester_id TEXT NOT NULL REFERENCES users(id),
    receiver_id TEXT NOT NULL REFERENCES users(id),
    status TEXT NOT NULL DEFAULT 'PENDING', -- FriendshipStatus enum
    created_at TEXT NOT NULL,
    accepted_at TEXT
);

CREATE INDEX idx_friendships_receiver ON friendships(receiver_id);
CREATE INDEX idx_friendships_status ON friendships(status);
```

## Social — Chat

```sql
CREATE TABLE chat_conversations (
    id TEXT PRIMARY KEY,
    participant_a TEXT NOT NULL REFERENCES users(id),
    participant_b TEXT NOT NULL REFERENCES users(id),
    created_at TEXT NOT NULL,
    last_message_at TEXT
);

CREATE INDEX idx_conversations_participant_a ON chat_conversations(participant_a);
CREATE INDEX idx_conversations_participant_b ON chat_conversations(participant_b);

CREATE TABLE chat_messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL REFERENCES chat_conversations(id),
    sender_id TEXT NOT NULL REFERENCES users(id),
    message_type TEXT NOT NULL, -- ChatMessageType enum
    text_content TEXT,
    shared_content_id TEXT,
    shared_content_type TEXT, -- SharedContentType enum
    created_at TEXT NOT NULL
);

CREATE INDEX idx_messages_conversation ON chat_messages(conversation_id, created_at);
```

## Moderation

```sql
CREATE TABLE moderation_reviews (
    id TEXT PRIMARY KEY,
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL, -- ContentEntityType enum
    moderator_id TEXT REFERENCES users(id),
    decision TEXT, -- ModerationDecision enum
    reason TEXT,
    is_post_publication INTEGER NOT NULL DEFAULT 0, -- boolean
    created_at TEXT NOT NULL,
    decided_at TEXT
);

CREATE INDEX idx_reviews_pending ON moderation_reviews(decision) WHERE decision IS NULL;

CREATE TABLE content_reports (
    id TEXT PRIMARY KEY,
    reporter_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL, -- ContentEntityType enum
    reason TEXT NOT NULL, -- ReportReason enum
    status TEXT NOT NULL DEFAULT 'PENDING', -- ReportStatus enum
    created_at TEXT NOT NULL
);

CREATE INDEX idx_reports_status ON content_reports(status);

CREATE TABLE strikes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    moderator_id TEXT NOT NULL REFERENCES users(id),
    reason TEXT NOT NULL,
    cooldown_until TEXT NOT NULL, -- created_at + 7 days
    created_at TEXT NOT NULL
);

CREATE INDEX idx_strikes_user ON strikes(user_id);
CREATE INDEX idx_strikes_cooldown ON strikes(user_id, cooldown_until);
```

## Creator Licensing

```sql
CREATE TABLE creator_license_requests (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    status TEXT NOT NULL DEFAULT 'PENDING', -- LicenseRequestStatus enum
    moderator_id TEXT REFERENCES users(id),
    rejection_reason TEXT,
    created_at TEXT NOT NULL,
    decided_at TEXT
);

CREATE INDEX idx_license_requests_status ON creator_license_requests(status);
```

## Gamification

```sql
CREATE TABLE badges (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL,
    icon_url TEXT NOT NULL,
    criteria TEXT NOT NULL
);

CREATE TABLE user_badges (
    user_id TEXT NOT NULL REFERENCES users(id),
    badge_id TEXT NOT NULL REFERENCES badges(id),
    awarded_at TEXT NOT NULL,
    PRIMARY KEY (user_id, badge_id)
);
```

## Notifications

```sql
CREATE TABLE notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    type TEXT NOT NULL, -- NotificationType enum
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    reference_id TEXT,
    reference_type TEXT,
    read INTEGER NOT NULL DEFAULT 0, -- boolean
    created_at TEXT NOT NULL
);

CREATE INDEX idx_notifications_user ON notifications(user_id, created_at);
CREATE INDEX idx_notifications_unread ON notifications(user_id, read) WHERE read = 0;
```

## Search History

```sql
CREATE TABLE search_history (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    query TEXT NOT NULL,
    searched_at TEXT NOT NULL
);

CREATE INDEX idx_search_history_user ON search_history(user_id, searched_at);
```

## Content View Tracking (for analytics)

```sql
CREATE TABLE content_views (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL, -- VIDEO or QUIZ
    viewed_at TEXT NOT NULL
);

CREATE INDEX idx_views_content ON content_views(content_id, content_type);
```

## Content Shares (for analytics)

```sql
CREATE TABLE content_shares (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL,
    shared_at TEXT NOT NULL
);

CREATE INDEX idx_shares_content ON content_shares(content_id, content_type);
```

---

## Notes

- All enums are stored as TEXT and validated at the application layer.
- Booleans use INTEGER (0/1) per SQLite convention.
- No foreign key enforcement by default in SQLite — enable with `PRAGMA foreign_keys = ON`.
- Indexes are tuned for expected MVP query patterns (feed queries, moderation queues, user lookups).
- Leaderboard data is computed via queries on `user_profiles.total_points`, not stored in a separate table.
