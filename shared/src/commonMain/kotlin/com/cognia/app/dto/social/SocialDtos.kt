package com.cognia.app.dto.social

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class FollowerListResponse(
    val followers: List<UserSummary>,
    val nextCursor: String? = null
)

@Serializable
data class FollowingListResponse(
    val following: List<UserSummary>,
    val nextCursor: String? = null
)

@Serializable
data class FriendRequestCreate(val userId: String)

@Serializable
data class FriendRequestAction(val action: String)

@Serializable
data class FriendListResponse(
    val friends: List<UserSummary>,
    val nextCursor: String? = null
)
