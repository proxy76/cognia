package com.cognia.app.ui.reel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    init {
        loadVideos()
    }

    fun loadVideos() {
        // TODO: Wire to API
        _state.value = ReelUiState(
            videos = listOf(
                VideoItem("1", "Intro to Quantum Physics", "Dr. Sarah", "u1", null, null, true),
                VideoItem("2", "History of Rome", "HistoryBuff", "u2", null, null, false),
                VideoItem("3", "Learn Guitar Basics", "MusicMaster", "u3", null, null, true),
                VideoItem("4", "Python for Beginners", "CodeAcademy", "u4", null, null, true),
                VideoItem("5", "Abstract Art Explained", "ArtLover", "u5", null, null, false)
            )
        )
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
