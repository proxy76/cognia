-- V1__initial_schema.sql
-- Migrated from SQLite to PostgreSQL
-- All IDs remain TEXT (UUID strings), all timestamps remain TEXT (ISO-8601)
-- Booleans converted from INTEGER (0/1) to BOOLEAN
-- New video metadata and ELI5 columns added

-- ── Users & Profiles ────────────────────────────────────────────────

CREATE TABLE users (
    id TEXT PRIMARY KEY,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT,
    display_name TEXT NOT NULL,
    avatar_url TEXT,
    role TEXT NOT NULL DEFAULT 'LEARNER',
    auth_provider TEXT NOT NULL DEFAULT 'EMAIL',
    suspended_until TEXT,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);

CREATE TABLE user_profiles (
    user_id TEXT PRIMARY KEY REFERENCES users(id),
    self_description TEXT,
    level INTEGER NOT NULL DEFAULT 1,
    total_points INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE categories (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    slug TEXT NOT NULL UNIQUE
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

-- ── Videos ───────────────────────────────────────────────────────────

CREATE TABLE videos (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    title TEXT NOT NULL,
    description TEXT,
    category_id TEXT NOT NULL REFERENCES categories(id),
    video_url TEXT,
    thumbnail_url TEXT,
    raw_file_path TEXT,
    status TEXT NOT NULL DEFAULT 'DRAFT',
    difficulty TEXT,
    -- Video storage metadata
    mime_type VARCHAR(100),
    file_extension VARCHAR(20),
    file_size_bytes BIGINT,
    duration_seconds DOUBLE PRECISION,
    upload_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processing_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    processing_error TEXT,
    -- ELI5 secondary video (self-reference)
    eli5_video_id TEXT REFERENCES videos(id),
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL,
    published_at TEXT
);

CREATE INDEX idx_videos_creator_id ON videos(creator_id);
CREATE INDEX idx_videos_status ON videos(status);
CREATE INDEX idx_videos_category_id ON videos(category_id);
CREATE INDEX idx_videos_published_at ON videos(published_at);
CREATE INDEX idx_videos_eli5_video_id ON videos(eli5_video_id);

-- ── Quizzes ─────────────────────────────────────────────────────────

CREATE TABLE quizzes (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    video_id TEXT REFERENCES videos(id),
    title TEXT NOT NULL,
    quiz_type TEXT NOT NULL,
    category_id TEXT NOT NULL REFERENCES categories(id),
    status TEXT NOT NULL DEFAULT 'DRAFT',
    difficulty TEXT,
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
    "text" TEXT NOT NULL,
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

CREATE INDEX idx_quiz_attempts_user_id ON quiz_attempts(user_id);
CREATE INDEX idx_quiz_attempts_quiz_id ON quiz_attempts(quiz_id);

-- ── Social: Follows ─────────────────────────────────────────────────

CREATE TABLE follows (
    follower_id TEXT NOT NULL REFERENCES users(id),
    followed_id TEXT NOT NULL REFERENCES users(id),
    created_at TEXT NOT NULL,
    PRIMARY KEY (follower_id, followed_id)
);

CREATE INDEX idx_follows_followed_id ON follows(followed_id);

-- ── Social: Friendships ─────────────────────────────────────────────

CREATE TABLE friendships (
    id TEXT PRIMARY KEY,
    requester_id TEXT NOT NULL REFERENCES users(id),
    receiver_id TEXT NOT NULL REFERENCES users(id),
    status TEXT NOT NULL DEFAULT 'PENDING',
    created_at TEXT NOT NULL,
    accepted_at TEXT
);

CREATE INDEX idx_friendships_receiver_id ON friendships(receiver_id);
CREATE INDEX idx_friendships_status ON friendships(status);

-- ── Social: Chat ────────────────────────────────────────────────────

CREATE TABLE chat_conversations (
    id TEXT PRIMARY KEY,
    participant_a TEXT NOT NULL REFERENCES users(id),
    participant_b TEXT NOT NULL REFERENCES users(id),
    created_at TEXT NOT NULL,
    last_message_at TEXT
);

CREATE INDEX idx_chat_conversations_participant_a ON chat_conversations(participant_a);
CREATE INDEX idx_chat_conversations_participant_b ON chat_conversations(participant_b);

CREATE TABLE chat_messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL REFERENCES chat_conversations(id),
    sender_id TEXT NOT NULL REFERENCES users(id),
    message_type TEXT NOT NULL,
    text_content TEXT,
    shared_content_id TEXT,
    shared_content_type TEXT,
    created_at TEXT NOT NULL
);

CREATE INDEX idx_chat_messages_conversation_created ON chat_messages(conversation_id, created_at);

-- ── Moderation ──────────────────────────────────────────────────────

CREATE TABLE moderation_reviews (
    id TEXT PRIMARY KEY,
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL,
    moderator_id TEXT REFERENCES users(id),
    decision TEXT,
    reason TEXT,
    is_post_publication BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TEXT NOT NULL,
    decided_at TEXT
);

CREATE INDEX idx_moderation_reviews_decision ON moderation_reviews(decision);

CREATE TABLE content_reports (
    id TEXT PRIMARY KEY,
    reporter_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL,
    reason TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING',
    report_count INTEGER NOT NULL DEFAULT 1,
    ai_assessment TEXT,
    ai_confidence TEXT,
    resolution TEXT,
    resolved_at TEXT,
    created_at TEXT NOT NULL
);

CREATE INDEX idx_content_reports_status ON content_reports(status);
CREATE INDEX idx_content_reports_content ON content_reports(content_id, content_type);

CREATE TABLE strikes (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    moderator_id TEXT NOT NULL REFERENCES users(id),
    reason TEXT NOT NULL,
    cooldown_until TEXT NOT NULL,
    created_at TEXT NOT NULL
);

CREATE INDEX idx_strikes_user_id ON strikes(user_id);
CREATE INDEX idx_strikes_user_cooldown ON strikes(user_id, cooldown_until);

-- ── Creator Licensing ───────────────────────────────────────────────

CREATE TABLE creator_license_requests (
    id TEXT PRIMARY KEY,
    creator_id TEXT NOT NULL REFERENCES users(id),
    status TEXT NOT NULL DEFAULT 'PENDING',
    moderator_id TEXT REFERENCES users(id),
    rejection_reason TEXT,
    created_at TEXT NOT NULL,
    decided_at TEXT
);

CREATE INDEX idx_creator_license_requests_status ON creator_license_requests(status);

-- ── Gamification ────────────────────────────────────────────────────

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

-- ── Notifications ───────────────────────────────────────────────────

CREATE TABLE notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    type TEXT NOT NULL,
    title TEXT NOT NULL,
    body TEXT NOT NULL,
    reference_id TEXT,
    reference_type TEXT,
    read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TEXT NOT NULL
);

CREATE INDEX idx_notifications_user_created ON notifications(user_id, created_at);
CREATE INDEX idx_notifications_user_read ON notifications(user_id, read);

-- ── User Blocks ────────────────────────────────────────────────────

CREATE TABLE user_blocks (
    id TEXT PRIMARY KEY,
    blocker_id TEXT NOT NULL REFERENCES users(id),
    blocked_id TEXT NOT NULL REFERENCES users(id),
    created_at TEXT NOT NULL
);

CREATE UNIQUE INDEX idx_user_blocks_blocker_blocked ON user_blocks(blocker_id, blocked_id);
CREATE INDEX idx_user_blocks_blocked_id ON user_blocks(blocked_id);

-- ── Moderation Audit Log ───────────────────────────────────────────

CREATE TABLE moderation_audit_log (
    id TEXT PRIMARY KEY,
    moderator_id TEXT NOT NULL REFERENCES users(id),
    action TEXT NOT NULL,
    target_type TEXT NOT NULL,
    target_id TEXT NOT NULL,
    details TEXT,
    created_at TEXT NOT NULL
);

CREATE INDEX idx_moderation_audit_log_moderator ON moderation_audit_log(moderator_id);
CREATE INDEX idx_moderation_audit_log_created ON moderation_audit_log(created_at);

-- ── Content View Tracking ───────────────────────────────────────────

CREATE TABLE content_views (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL,
    viewed_at TEXT NOT NULL
);

CREATE INDEX idx_content_views_content ON content_views(content_id, content_type);

-- ── Content Shares ──────────────────────────────────────────────────

CREATE TABLE content_shares (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    content_id TEXT NOT NULL,
    content_type TEXT NOT NULL,
    shared_at TEXT NOT NULL
);

CREATE INDEX idx_content_shares_content ON content_shares(content_id, content_type);

-- ── Search History ──────────────────────────────────────────────────

CREATE TABLE search_history (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES users(id),
    query TEXT NOT NULL,
    searched_at TEXT NOT NULL
);

CREATE INDEX idx_search_history_user_searched ON search_history(user_id, searched_at);
