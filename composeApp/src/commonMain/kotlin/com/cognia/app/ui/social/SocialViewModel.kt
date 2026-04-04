package com.cognia.app.ui.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    fun follow(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.follow(targetUserId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isFollowing = result.data.following)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun unfollow(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.unfollow(targetUserId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, isFollowing = false)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun loadFollowers(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.getFollowers(userId)) {
                is ApiResult.Success -> {
                    val followers = result.data.followers.map {
                        UserSocialSummary(it.id, it.displayName, it.avatarUrl)
                    }
                    _state.value = _state.value.copy(isLoading = false, followers = followers)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, followers = emptyList())
                }
            }
        }
    }

    fun loadFollowing(userId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.getFollowing(userId)) {
                is ApiResult.Success -> {
                    val following = result.data.following.map {
                        UserSocialSummary(it.id, it.displayName, it.avatarUrl)
                    }
                    _state.value = _state.value.copy(isLoading = false, following = following)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, following = emptyList())
                }
            }
        }
    }

    fun sendFriendRequest(targetUserId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (api.sendFriendRequest(targetUserId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isLoading = false, friendRequestSent = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to send request")
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (api.acceptFriendRequest(requestId)) {
                is ApiResult.Success -> {
                    val updatedRequests = _state.value.pendingRequests.filter { it.id != requestId }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        friendRequestAccepted = true,
                        pendingRequests = updatedRequests
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to accept")
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (api.declineFriendRequest(requestId)) {
                is ApiResult.Success -> {
                    val updatedRequests = _state.value.pendingRequests.filter { it.id != requestId }
                    _state.value = _state.value.copy(isLoading = false, pendingRequests = updatedRequests)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Failed to decline")
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error")
                }
            }
        }
    }

    fun loadFriends() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.getFriends()) {
                is ApiResult.Success -> {
                    val friends = result.data.friends.map {
                        UserSocialSummary(it.id, it.displayName, it.avatarUrl)
                    }
                    _state.value = _state.value.copy(isLoading = false, friends = friends)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, friends = emptyList())
                }
            }
        }
    }

    fun loadPendingRequests() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.getPendingFriendRequests()) {
                is ApiResult.Success -> {
                    val requests = result.data.requests.map { req ->
                        FriendRequestUi(
                            id = req.id,
                            requester = UserSocialSummary(
                                req.requester.id,
                                req.requester.displayName,
                                req.requester.avatarUrl
                            ),
                            status = req.status,
                            createdAt = req.createdAt
                        )
                    }
                    _state.value = _state.value.copy(isLoading = false, pendingRequests = requests)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, pendingRequests = emptyList())
                }
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
