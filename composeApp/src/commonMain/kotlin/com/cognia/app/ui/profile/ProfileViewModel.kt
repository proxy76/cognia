package com.cognia.app.ui.profile

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    init {
        loadProfile()
    }

    fun loadProfile() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/users/me
        // For now, show mock data
        _state.value = ProfileUiState(
            isLoading = false,
            displayName = "Cognia User",
            role = "LEARNER",
            level = 1,
            totalPoints = 0,
            badgeCount = 0,
            followerCount = 0,
            followingCount = 0,
            friendCount = 0
        )
    }

    fun retry() {
        loadProfile()
    }
}
