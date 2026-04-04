package com.cognia.app.dto.moderation

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class BlockedUserResponse(
    val id: String,
    val blockedUser: UserSummary,
    val createdAt: String
)

@Serializable
data class BlockedUserListResponse(
    val users: List<BlockedUserResponse>
)

@Serializable
data class ReportResolveRequest(
    val status: String,
    val resolution: String? = null
)

@Serializable
data class ReportStatsResponse(
    val pending: Long,
    val reviewed: Long,
    val dismissed: Long,
    val byContentType: Map<String, Long>
)

@Serializable
data class SuspendUserRequest(
    val durationDays: Int,
    val reason: String
)

@Serializable
data class AuditLogEntry(
    val id: String,
    val moderatorId: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val details: String?,
    val createdAt: String
)

@Serializable
data class AuditLogResponse(
    val entries: List<AuditLogEntry>
)
