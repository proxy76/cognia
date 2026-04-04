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
    val reason: String? = null,
    val issueStrike: Boolean = false
)

@Serializable
data class ModerationReviewResponse(
    val id: String,
    val contentId: String,
    val contentType: String,
    val moderatorId: String?,
    val decision: String?,
    val reason: String?,
    val isPostPublication: Boolean,
    val createdAt: String,
    val decidedAt: String?
)

@Serializable
data class ModerationReviewListResponse(
    val items: List<ModerationReviewResponse>
)

@Serializable
data class ReportCreateRequest(
    val contentId: String,
    val contentType: String,
    val reason: String
)

@Serializable
data class ReportResponse(
    val id: String,
    val reporterId: String,
    val contentId: String,
    val contentType: String,
    val reason: String,
    val status: String,
    val createdAt: String
)

@Serializable
data class ReportListResponse(
    val items: List<ReportResponse>
)

@Serializable
data class StrikeCreateRequest(
    val userId: String,
    val reason: String
)

@Serializable
data class StrikeResponse(
    val id: String,
    val userId: String,
    val moderatorId: String,
    val reason: String,
    val cooldownUntil: String,
    val createdAt: String
)

@Serializable
data class LicenseRequestResponse(
    val id: String,
    val creatorId: String,
    val status: String,
    val moderatorId: String? = null,
    val rejectionReason: String? = null,
    val createdAt: String,
    val decidedAt: String? = null
)

@Serializable
data class LicenseRequestListResponse(
    val items: List<LicenseRequestResponse>
)

@Serializable
data class LicenseRejectRequest(
    val reason: String? = null
)
