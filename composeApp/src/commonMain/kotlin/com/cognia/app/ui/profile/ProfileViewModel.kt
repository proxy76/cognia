package com.cognia.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val displayName: String = "",
    val avatarUrl: String? = null,
    val role: String = "LEARNER",
    val level: Int = 1,
    val totalPoints: Int = 0,
    val badgeCount: Int = 0,
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val friendCount: Int = 0,
    val selfDescription: String? = null
)

class ProfileViewModel : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadProfile()
    }

    fun loadProfile() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getMyProfile()) {
                is ApiResult.Success -> {
                    val profile = result.data
                    _state.value = ProfileUiState(
                        isLoading = false,
                        displayName = profile.displayName,
                        avatarUrl = profile.avatarUrl,
                        role = profile.role,
                        level = profile.level,
                        totalPoints = profile.totalPoints,
                        badgeCount = profile.badgeCount,
                        followerCount = profile.followerCount,
                        followingCount = profile.followingCount,
                        friendCount = profile.friendCount,
                        selfDescription = profile.selfDescription
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Network error: ${result.throwable.message}"
                    )
                }
            }
        }
    }

    fun retry() {
        loadProfile()
    }
}
