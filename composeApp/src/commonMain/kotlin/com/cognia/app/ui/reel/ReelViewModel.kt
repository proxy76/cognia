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

/** A reel page is either a video or an inline quiz. */
sealed class ReelPage {
    abstract val key: String
}

data class VideoPage(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val streamUrl: String,
    val eli5StreamUrl: String?,
    val hasQuiz: Boolean = false,
    val quizId: String? = null,
    override val key: String = "video-$id"
) : ReelPage()

data class QuizPage(
    val quizId: String,
    val videoTitle: String,
    override val key: String = "quiz-$quizId"
) : ReelPage()

data class ReelUiState(
    val pages: List<ReelPage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Set of video IDs currently in ELI5 mode */
    val eli5Videos: Set<String> = emptySet(),
)

class ReelViewModel : ViewModel() {
    private val _state = MutableStateFlow(ReelUiState())
    val state: StateFlow<ReelUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadVideos()
    }

    fun loadVideos() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = api.getForYouFeed(page = 1, limit = 20)) {
                is ApiResult.Success -> {
                    val pages = mutableListOf<ReelPage>()
                    for (item in result.data.items) {
                        val streamUrl = ApiConfig.apiUrl("/videos/${item.id}/stream")
                        val eli5Url = if (item.hasEli5) ApiConfig.apiUrl("/videos/${item.id}/eli5-stream") else null
                        pages.add(
                            VideoPage(
                                id = item.id,
                                title = item.title,
                                creatorName = item.creatorName,
                                creatorId = item.creatorId,
                                streamUrl = streamUrl,
                                eli5StreamUrl = eli5Url,
                                hasQuiz = item.hasQuiz,
                                quizId = item.quizId,
                            )
                        )
                        val qId = item.quizId
                        if (item.hasQuiz && qId != null) {
                            pages.add(QuizPage(quizId = qId, videoTitle = item.title))
                        }
                    }
                    _state.value = ReelUiState(pages = pages, isLoading = false)
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

    fun toggleEli5(videoId: String) {
        val current = _state.value
        val newSet = if (videoId in current.eli5Videos) {
            current.eli5Videos - videoId
        } else {
            current.eli5Videos + videoId
        }
        _state.value = current.copy(eli5Videos = newSet)
    }

    fun isEli5(videoId: String): Boolean = videoId in _state.value.eli5Videos
}
