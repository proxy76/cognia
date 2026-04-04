package com.cognia.app.ui.social

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SocialUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFollowing: Boolean = false,
    val followers: List<UserSocialSummary> = emptyList(),
    val following: List<UserSocialSummary> = emptyList(),
    val friends: List<UserSocialSummary> = emptyList(),
    val pendingRequests: List<FriendRequestUi> = emptyList(),
    val friendRequestSent: Boolean = false,
    val friendRequestAccepted: Boolean = false
)

data class UserSocialSummary(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null
)

data class FriendRequestUi(
    val id: String,
    val requester: UserSocialSummary,
    val status: String,
    val createdAt: String
)

class SocialViewModel : ViewModel() {
    private val _state = MutableStateFlow(SocialUiState())
    val state: StateFlow<SocialUiState> = _state.asStateFlow()

    fun follow(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call POST /api/v1/users/{id}/follow
        _state.value = _state.value.copy(isLoading = false, isFollowing = true)
    }

    fun unfollow(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call DELETE /api/v1/users/{id}/follow
        _state.value = _state.value.copy(isLoading = false, isFollowing = false)
    }

    fun loadFollowers(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/users/{id}/followers
        _state.value = _state.value.copy(isLoading = false, followers = emptyList())
    }

    fun loadFollowing(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/users/{id}/following
        _state.value = _state.value.copy(isLoading = false, following = emptyList())
    }

    fun sendFriendRequest(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call POST /api/v1/friends/request/{userId}
        _state.value = _state.value.copy(isLoading = false, friendRequestSent = true)
    }

    fun acceptFriendRequest(requestId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call POST /api/v1/friends/accept/{requestId}
        val updatedRequests = _state.value.pendingRequests.filter { it.id != requestId }
        _state.value = _state.value.copy(
            isLoading = false,
            friendRequestAccepted = true,
            pendingRequests = updatedRequests
        )
    }

    fun declineFriendRequest(requestId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call POST /api/v1/friends/decline/{requestId}
        val updatedRequests = _state.value.pendingRequests.filter { it.id != requestId }
        _state.value = _state.value.copy(isLoading = false, pendingRequests = updatedRequests)
    }

    fun loadFriends() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/friends
        _state.value = _state.value.copy(isLoading = false, friends = emptyList())
    }

    fun loadPendingRequests() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/friends/requests
        _state.value = _state.value.copy(isLoading = false, pendingRequests = emptyList())
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
