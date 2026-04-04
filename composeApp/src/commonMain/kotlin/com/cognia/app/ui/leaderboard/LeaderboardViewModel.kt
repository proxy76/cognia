package com.cognia.app.ui.leaderboard

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val totalPoints: Long,
    val level: Int
)

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val entries: List<LeaderboardEntry> = emptyList()
)

class LeaderboardViewModel : ViewModel() {
    private val _state = MutableStateFlow(LeaderboardUiState())
    val state: StateFlow<LeaderboardUiState> = _state.asStateFlow()

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/leaderboard
        // For now, show empty state
        _state.value = LeaderboardUiState(
            isLoading = false,
            entries = emptyList()
        )
    }

    fun retry() {
        loadLeaderboard()
    }
}
