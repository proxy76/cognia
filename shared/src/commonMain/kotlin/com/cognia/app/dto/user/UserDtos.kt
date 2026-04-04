package com.cognia.app.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String,
    val level: Int,
    val totalPoints: Long,
    val categories: List<CategorySummary> = emptyList(),
    val badgeCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val friendCount: Int = 0
)

@Serializable
data class PublicUserResponse(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String,
    val level: Int,
    val followerCount: Int = 0,
    val videoCount: Int = 0,
    val badges: List<BadgeSummary> = emptyList()
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null
)

@Serializable
data class CategorySummary(val id: String, val name: String)

@Serializable
data class BadgeSummary(val id: String, val name: String, val iconUrl: String)

@Serializable
data class UserSummary(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null
)
