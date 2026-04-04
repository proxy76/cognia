package com.cognia.app.dto.moderation

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class ModerationQueueItem(
    val reviewId: String,
    val contentType: String,
    val contentId: String,
    val title: String,
    val creator: UserSummary,
    val isPostPublication: Boolean,
    val submittedAt: String
)

@Serializable
data class ModerationQueueResponse(
    val items: List<ModerationQueueItem>
)

@Serializable
data class ModerationDecisionRequest(
    val decision: String,
    val reason: String? = null
)

@Serializable
data class ReportCreateRequest(
    val contentId: String,
    val contentType: String,
    val reason: String
)

@Serializable
data class ReportQueueItem(
    val id: String,
    val reporterId: String,
    val contentId: String,
    val contentType: String,
    val reason: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class ReportQueueResponse(
    val items: List<ReportQueueItem>
)

@Serializable
data class ReportReviewRequest(
    val action: String,
    val reason: String? = null
)

@Serializable
data class StrikeCreateRequest(
    val userId: String,
    val reason: String
)

@Serializable
data class LicenseRequestResponse(
    val id: String,
    val creatorId: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class LicenseDecisionRequest(
    val decision: String,
    val reason: String? = null
)
