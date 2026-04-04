package com.cognia.app.dto.profile

import kotlinx.serialization.Serializable

@Serializable
data class UserProfileResponse(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val selfDescription: String?,
    val level: Int,
    val totalPoints: Int,
    val categories: List<String>,
    val badgeCount: Int,
    val followerCount: Int,
    val followingCount: Int,
    val friendCount: Int
)

@Serializable
data class PublicProfileResponse(
    val id: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val level: Int,
    val followerCount: Int,
    val videoCount: Int,
    val badges: List<BadgeSummary>
)

@Serializable
data class BadgeSummary(
    val id: String,
    val name: String,
    val iconUrl: String
)

@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val selfDescription: String? = null
)
