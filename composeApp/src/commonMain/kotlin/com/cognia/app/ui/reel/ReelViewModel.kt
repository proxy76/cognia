package com.cognia.app.ui.reel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiConfig
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReelUiState(
    val currentIndex: Int = 0,
    val videos: List<VideoItem> = emptyList(),
    val isPlaying: Boolean = true,
    val isBuffering: Boolean = false,
    val error: String? = null
)

data class VideoItem(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val videoUrl: String?,
    val thumbnailUrl: String?,
    val hasQuiz: Boolean = false
)

class ReelViewModel : ViewModel() {
    private val _state = MutableStateFlow(ReelUiState())
    val state: StateFlow<ReelUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            when (val result = api.getForYouFeed(page = 1, limit = 20)) {
                is ApiResult.Success -> {
                    val videos = result.data.items.map { item ->
                        VideoItem(
                            id = item.id,
                            title = item.title,
                            creatorName = item.creatorName,
                            creatorId = item.creatorId,
                            videoUrl = "${ApiConfig.baseUrl}${ApiConfig.API_PREFIX}/videos/${item.id}/stream",
                            thumbnailUrl = "${ApiConfig.baseUrl}${ApiConfig.API_PREFIX}/videos/${item.id}/thumbnail",
                            hasQuiz = item.hasQuiz
                        )
                    }
                    _state.value = ReelUiState(videos = videos)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(error = "Network error: ${result.throwable.message}")
                }
            }
        }
    }

    fun swipeToNext() {
        val current = _state.value
        if (current.currentIndex < current.videos.size - 1) {
            _state.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun swipeToPrevious() {
        val current = _state.value
        if (current.currentIndex > 0) {
            _state.value = current.copy(currentIndex = current.currentIndex - 1)
        }
    }

    fun togglePlayPause() {
        val current = _state.value
        _state.value = current.copy(isPlaying = !current.isPlaying)
    }

    fun onBuffering(isBuffering: Boolean) {
        _state.value = _state.value.copy(isBuffering = isBuffering)
    }
}
