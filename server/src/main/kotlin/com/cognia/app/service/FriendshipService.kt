package com.cognia.app.service

import com.cognia.app.dto.social.FriendListResponse
import com.cognia.app.dto.social.FriendRequestResponse
import com.cognia.app.dto.social.PendingRequestsResponse
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.repository.FriendshipRepository

class FriendshipService(private val friendshipRepository: FriendshipRepository) {

    fun sendRequest(requesterId: String, receiverId: String): FriendRequestResponse {
        require(requesterId != receiverId) { "Cannot send a friend request to yourself" }

        val existing = friendshipRepository.findExisting(requesterId, receiverId)
        if (existing != null) {
            when (existing.status) {
                "ACCEPTED" -> throw FriendshipAlreadyExistsException()
                "PENDING" -> throw FriendRequestAlreadyPendingException()
            }
        }

        val row = friendshipRepository.sendRequest(requesterId, receiverId)
        // Return a simplified response; the caller may not have user details here
        return FriendRequestResponse(
            id = row.id,
            requester = UserSummary(id = row.requesterId, displayName = "", avatarUrl = null),
            receiver = UserSummary(id = row.receiverId, displayName = "", avatarUrl = null),
            status = row.status,
            createdAt = row.createdAt
        )
    }

    fun acceptRequest(requestId: String, currentUserId: String): FriendRequestResponse {
        val friendship = friendshipRepository.findById(requestId)
            ?: throw FriendRequestNotFoundException()
        require(friendship.receiverId == currentUserId) { "Only the receiver can accept a friend request" }
        require(friendship.status == "PENDING") { "Friend request is not pending" }

        friendshipRepository.acceptRequest(requestId)
        val updated = friendshipRepository.findById(requestId)!!
        return FriendRequestResponse(
            id = updated.id,
            requester = UserSummary(id = updated.requesterId, displayName = "", avatarUrl = null),
            receiver = UserSummary(id = updated.receiverId, displayName = "", avatarUrl = null),
            status = updated.status,
            createdAt = updated.createdAt
        )
    }

    fun declineRequest(requestId: String, currentUserId: String): FriendRequestResponse {
        val friendship = friendshipRepository.findById(requestId)
            ?: throw FriendRequestNotFoundException()
        require(friendship.receiverId == currentUserId) { "Only the receiver can decline a friend request" }
        require(friendship.status == "PENDING") { "Friend request is not pending" }

        friendshipRepository.declineRequest(requestId)
        val updated = friendshipRepository.findById(requestId)!!
        return FriendRequestResponse(
            id = updated.id,
            requester = UserSummary(id = updated.requesterId, displayName = "", avatarUrl = null),
            receiver = UserSummary(id = updated.receiverId, displayName = "", avatarUrl = null),
            status = updated.status,
            createdAt = updated.createdAt
        )
    }

    fun getFriends(userId: String): FriendListResponse {
        val friends = friendshipRepository.getFriends(userId).map {
            UserSummary(id = it.id, displayName = it.displayName, avatarUrl = it.avatarUrl)
        }
        return FriendListResponse(friends = friends)
    }

    fun getPendingRequests(userId: String): PendingRequestsResponse {
        val requests = friendshipRepository.getPendingRequests(userId).map {
            FriendRequestResponse(
                id = it.id,
                requester = UserSummary(
                    id = it.requesterId,
                    displayName = it.requesterDisplayName,
                    avatarUrl = it.requesterAvatarUrl
                ),
                receiver = UserSummary(
                    id = it.receiverId,
                    displayName = it.receiverDisplayName,
                    avatarUrl = it.receiverAvatarUrl
                ),
                status = it.status,
                createdAt = it.createdAt
            )
        }
        return PendingRequestsResponse(requests = requests)
    }

    fun areFriends(userA: String, userB: String): Boolean {
        return friendshipRepository.areFriends(userA, userB)
    }
}

class FriendshipAlreadyExistsException : RuntimeException("Already friends")
class FriendRequestAlreadyPendingException : RuntimeException("Friend request already pending")
class FriendRequestNotFoundException : RuntimeException("Friend request not found")
