package com.cognia.app.ui.analytics

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class VideoStats(
    val videoId: String,
    val title: String,
    val viewCount: Long
)

data class AnalyticsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val totalViews: Long = 0,
    val totalQuizAttempts: Long = 0,
    val averageQuizScore: Double = 0.0,
    val videoStats: List<VideoStats> = emptyList()
)

class AnalyticsViewModel : ViewModel() {
    private val _state = MutableStateFlow(AnalyticsUiState())
    val state: StateFlow<AnalyticsUiState> = _state.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/analytics/creator
        // For now, show empty state
        _state.value = AnalyticsUiState(
            isLoading = false,
            totalViews = 0,
            totalQuizAttempts = 0,
            averageQuizScore = 0.0,
            videoStats = emptyList()
        )
    }

    fun retry() {
        loadAnalytics()
    }
}
