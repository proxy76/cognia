package com.cognia.app.database

import org.jetbrains.exposed.sql.Table

// ── Users & Profiles ────────────────────────────────────────────────

object UsersTable : Table("users") {
    val id = text("id")
    val email = text("email").uniqueIndex()
    val passwordHash = text("password_hash").nullable()
    val displayName = text("display_name")
    val avatarUrl = text("avatar_url").nullable()
    val role = text("role").default("LEARNER")
    val authProvider = text("auth_provider").default("EMAIL")
    val suspendedUntil = text("suspended_until").nullable()
    val createdAt = text("created_at")
    val updatedAt = text("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object UserProfilesTable : Table("user_profiles") {
    val userId = text("user_id").references(UsersTable.id)
    val selfDescription = text("self_description").nullable()
    val level = integer("level").default(1)
    val totalPoints = integer("total_points").default(0)
    override val primaryKey = PrimaryKey(userId)
}

object UserCategoriesTable : Table("user_categories") {
    val userId = text("user_id").references(UsersTable.id)
    val categoryId = text("category_id").references(CategoriesTable.id)
    override val primaryKey = PrimaryKey(userId, categoryId)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val tokenHash = text("token_hash").uniqueIndex()
    val expiresAt = text("expires_at")
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)
}

// ── Categories ──────────────────────────────────────────────────────

object CategoriesTable : Table("categories") {
    val id = text("id")
    val name = text("name").uniqueIndex()
    val slug = text("slug").uniqueIndex()
    override val primaryKey = PrimaryKey(id)
}

// ── Videos ───────────────────────────────────────────────────────────

object VideosTable : Table("videos") {
    val id = text("id")
    val creatorId = text("creator_id").references(UsersTable.id)
    val title = text("title")
    val description = text("description").nullable()
    val categoryId = text("category_id").references(CategoriesTable.id)
    val videoUrl = text("video_url").nullable()
    val thumbnailUrl = text("thumbnail_url").nullable()
    val rawFilePath = text("raw_file_path").nullable()
    val status = text("status").default("DRAFT")
    val difficulty = text("difficulty").nullable()
    val mimeType = varchar("mime_type", 100).nullable()
    val fileExtension = varchar("file_extension", 20).nullable()
    val fileSizeBytes = long("file_size_bytes").nullable()
    val durationSeconds = double("duration_seconds").nullable()
    val uploadStatus = varchar("upload_status", 20).default("PENDING")
    val processingStatus = varchar("processing_status", 20).default("PENDING")
    val processingError = text("processing_error").nullable()
    val eli5VideoId = text("eli5_video_id").references(id).nullable()
    val createdAt = text("created_at")
    val updatedAt = text("updated_at")
    val publishedAt = text("published_at").nullable()
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, creatorId)
        index(false, status)
        index(false, categoryId)
        index(false, publishedAt)
        index(false, eli5VideoId)
    }
}

// ── Quizzes ─────────────────────────────────────────────────────────

object QuizzesTable : Table("quizzes") {
    val id = text("id")
    val creatorId = text("creator_id").references(UsersTable.id)
    val videoId = text("video_id").references(VideosTable.id).nullable()
    val title = text("title")
    val quizType = text("quiz_type")
    val categoryId = text("category_id").references(CategoriesTable.id)
    val status = text("status").default("DRAFT")
    val difficulty = text("difficulty").nullable()
    val createdAt = text("created_at")
    val updatedAt = text("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object QuizQuestionsTable : Table("quiz_questions") {
    val id = text("id")
    val quizId = text("quiz_id").references(QuizzesTable.id)
    val questionText = text("question_text")
    val correctOptionIndex = integer("correct_option_index")
    val orderIndex = integer("order_index")
    override val primaryKey = PrimaryKey(id)
}

object QuizOptionsTable : Table("quiz_options") {
    val id = text("id")
    val questionId = text("question_id").references(QuizQuestionsTable.id)
    val text = text("text")
    val orderIndex = integer("order_index")
    override val primaryKey = PrimaryKey(id)
}

object QuizAttemptsTable : Table("quiz_attempts") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val quizId = text("quiz_id").references(QuizzesTable.id)
    val score = integer("score")
    val totalQuestions = integer("total_questions")
    val pointsAwarded = integer("points_awarded")
    val completedAt = text("completed_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, userId)
        index(false, quizId)
    }
}

// ── Social: Follows ─────────────────────────────────────────────────

object FollowsTable : Table("follows") {
    val followerId = text("follower_id").references(UsersTable.id)
    val followedId = text("followed_id").references(UsersTable.id)
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(followerId, followedId)

    init {
        index(false, followedId)
    }
}

// ── Social: Friendships ─────────────────────────────────────────────

object FriendshipsTable : Table("friendships") {
    val id = text("id")
    val requesterId = text("requester_id").references(UsersTable.id)
    val receiverId = text("receiver_id").references(UsersTable.id)
    val status = text("status").default("PENDING")
    val createdAt = text("created_at")
    val acceptedAt = text("accepted_at").nullable()
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, receiverId)
        index(false, status)
    }
}

// ── Social: Chat ────────────────────────────────────────────────────

object ChatConversationsTable : Table("chat_conversations") {
    val id = text("id")
    val participantA = text("participant_a").references(UsersTable.id)
    val participantB = text("participant_b").references(UsersTable.id)
    val createdAt = text("created_at")
    val lastMessageAt = text("last_message_at").nullable()
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, participantA)
        index(false, participantB)
    }
}

object ChatMessagesTable : Table("chat_messages") {
    val id = text("id")
    val conversationId = text("conversation_id").references(ChatConversationsTable.id)
    val senderId = text("sender_id").references(UsersTable.id)
    val messageType = text("message_type")
    val textContent = text("text_content").nullable()
    val sharedContentId = text("shared_content_id").nullable()
    val sharedContentType = text("shared_content_type").nullable()
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, conversationId, createdAt)
    }
}

// ── Moderation ──────────────────────────────────────────────────────

object ModerationReviewsTable : Table("moderation_reviews") {
    val id = text("id")
    val contentId = text("content_id")
    val contentType = text("content_type")
    val moderatorId = text("moderator_id").references(UsersTable.id).nullable()
    val decision = text("decision").nullable()
    val reason = text("reason").nullable()
    val isPostPublication = bool("is_post_publication").default(false)
    val createdAt = text("created_at")
    val decidedAt = text("decided_at").nullable()
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, decision)
    }
}

object ContentReportsTable : Table("content_reports") {
    val id = text("id")
    val reporterId = text("reporter_id").references(UsersTable.id)
    val contentId = text("content_id")
    val contentType = text("content_type")
    val reason = text("reason")
    val status = text("status").default("PENDING")
    val reportCount = integer("report_count").default(1)
    val aiAssessment = text("ai_assessment").nullable()
    val aiConfidence = text("ai_confidence").nullable()
    val resolution = text("resolution").nullable()
    val resolvedAt = text("resolved_at").nullable()
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, status)
        index(false, contentId, contentType)
    }
}

object StrikesTable : Table("strikes") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val moderatorId = text("moderator_id").references(UsersTable.id)
    val reason = text("reason")
    val cooldownUntil = text("cooldown_until")
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, userId)
        index(false, userId, cooldownUntil)
    }
}

// ── Creator Licensing ───────────────────────────────────────────────

object CreatorLicenseRequestsTable : Table("creator_license_requests") {
    val id = text("id")
    val creatorId = text("creator_id").references(UsersTable.id)
    val status = text("status").default("PENDING")
    val moderatorId = text("moderator_id").references(UsersTable.id).nullable()
    val rejectionReason = text("rejection_reason").nullable()
    val createdAt = text("created_at")
    val decidedAt = text("decided_at").nullable()
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, status)
    }
}

// ── Gamification ────────────────────────────────────────────────────

object BadgesTable : Table("badges") {
    val id = text("id")
    val name = text("name").uniqueIndex()
    val description = text("description")
    val iconUrl = text("icon_url")
    val criteria = text("criteria")
    override val primaryKey = PrimaryKey(id)
}

object UserBadgesTable : Table("user_badges") {
    val userId = text("user_id").references(UsersTable.id)
    val badgeId = text("badge_id").references(BadgesTable.id)
    val awardedAt = text("awarded_at")
    override val primaryKey = PrimaryKey(userId, badgeId)
}

// ── Notifications ───────────────────────────────────────────────────

object NotificationsTable : Table("notifications") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val type = text("type")
    val title = text("title")
    val body = text("body")
    val referenceId = text("reference_id").nullable()
    val referenceType = text("reference_type").nullable()
    val read = bool("read").default(false)
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, userId, createdAt)
        index(false, userId, read)
    }
}

// ── Search History ──────────────────────────────────────────────────

object SearchHistoryTable : Table("search_history") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val query = text("query")
    val searchedAt = text("searched_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, userId, searchedAt)
    }
}

// ── User Blocks ────────────────────────────────────────────────────

object UserBlocksTable : Table("user_blocks") {
    val id = text("id")
    val blockerId = text("blocker_id").references(UsersTable.id)
    val blockedId = text("blocked_id").references(UsersTable.id)
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(blockerId, blockedId)
        index(false, blockedId)
    }
}

// ── Moderation Audit Log ───────────────────────────────────────────

object ModerationAuditLogTable : Table("moderation_audit_log") {
    val id = text("id")
    val moderatorId = text("moderator_id").references(UsersTable.id)
    val action = text("action")
    val targetType = text("target_type")
    val targetId = text("target_id")
    val details = text("details").nullable()
    val createdAt = text("created_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, moderatorId)
        index(false, createdAt)
    }
}

// ── Content View Tracking ───────────────────────────────────────────

object ContentViewsTable : Table("content_views") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val contentId = text("content_id")
    val contentType = text("content_type")
    val viewedAt = text("viewed_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, contentId, contentType)
    }
}

// ── Content Shares ──────────────────────────────────────────────────

object ContentSharesTable : Table("content_shares") {
    val id = text("id")
    val userId = text("user_id").references(UsersTable.id)
    val contentId = text("content_id")
    val contentType = text("content_type")
    val sharedAt = text("shared_at")
    override val primaryKey = PrimaryKey(id)

    init {
        index(false, contentId, contentType)
    }
}
