package com.cognia.app.dto.social

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class FollowStatusResponse(
    val following: Boolean
)

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
data class FriendRequestResponse(
    val id: String,
    val requester: UserSummary,
    val receiver: UserSummary,
    val status: String,
    val createdAt: String
)

@Serializable
data class PendingRequestsResponse(
    val requests: List<FriendRequestResponse>
)

@Serializable
data class FriendListResponse(
    val friends: List<UserSummary>,
    val nextCursor: String? = null
)
