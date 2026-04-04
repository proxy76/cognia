package com.cognia.app.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    init {
        loadLeaderboard()
    }

    fun loadLeaderboard() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getLeaderboard()) {
                is ApiResult.Success -> {
                    val entries = result.data.entries.map { entry ->
                        LeaderboardEntry(
                            rank = entry.rank,
                            userId = entry.user.id,
                            displayName = entry.user.displayName,
                            avatarUrl = entry.user.avatarUrl,
                            totalPoints = entry.totalPoints,
                            level = entry.level
                        )
                    }
                    _state.value = LeaderboardUiState(isLoading = false, entries = entries)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, entries = emptyList())
                }
            }
        }
    }

    fun retry() {
        loadLeaderboard()
    }
}
