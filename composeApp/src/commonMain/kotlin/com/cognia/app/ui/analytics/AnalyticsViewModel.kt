package com.cognia.app.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    init {
        loadAnalytics()
    }

    fun loadAnalytics() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getCreatorAnalytics()) {
                is ApiResult.Success -> {
                    val data = result.data
                    _state.value = AnalyticsUiState(
                        isLoading = false,
                        totalViews = data.totalViews,
                        totalQuizAttempts = data.totalQuizAttempts,
                        averageQuizScore = data.averageQuizScore,
                        videoStats = data.videoStats.map { VideoStats(it.videoId, it.title, it.viewCount) }
                    )
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

    fun retry() {
        loadAnalytics()
    }
}
