package com.cognia.app.dto.gamification

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class LeaderboardEntryResponse(
    val rank: Int,
    val user: UserSummary,
    val totalPoints: Long,
    val level: Int
)

@Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntryResponse>
)

@Serializable
data class BadgeResponse(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val criteria: String
)

@Serializable
data class BadgeListResponse(
    val badges: List<BadgeResponse>
)

@Serializable
data class UserBadgeResponse(
    val badge: BadgeResponse,
    val awardedAt: String
)
