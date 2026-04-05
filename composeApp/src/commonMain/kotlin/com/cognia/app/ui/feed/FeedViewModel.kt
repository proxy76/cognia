package com.cognia.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class FeedTab { FOR_YOU, DEEP_DIVE }

data class FeedItemUi(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val thumbnailUrl: String?,
    val categoryName: String,
    val difficulty: String?,
    val hasQuiz: Boolean,
<<<<<<< Updated upstream
    val hasEli5: Boolean = false
=======
    val eli5VideoId: String? = null
>>>>>>> Stashed changes
)

/** A page in the feed pager: either a video card or an inline quiz card. */
sealed class FeedPageItem {
    data class Video(val item: FeedItemUi) : FeedPageItem()
    data class QuizCard(val videoId: String, val creatorName: String, val categoryName: String) : FeedPageItem()
}

data class FeedUiState(
    val selectedTab: FeedTab = FeedTab.FOR_YOU,
    val items: List<FeedItemUi> = emptyList(),
<<<<<<< Updated upstream
=======
    /** Pager pages: videos interleaved with quiz cards after videos that have quizzes. */
    val pages: List<FeedPageItem> = emptyList(),
    val currentIndex: Int = 0,
>>>>>>> Stashed changes
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val hasMore: Boolean = false
)

class FeedViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadFeed()
    }

    fun selectTab(tab: FeedTab) {
        _state.value = _state.value.copy(selectedTab = tab, page = 1)
        loadFeed()
    }

    fun loadFeed() {
        val tab = _state.value.selectedTab
        val page = _state.value.page
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val result = when (tab) {
                FeedTab.FOR_YOU -> api.getForYouFeed(page)
                FeedTab.DEEP_DIVE -> api.getDeepDiveFeed(page)
            }
            when (result) {
                is ApiResult.Success -> {
                    val items = result.data.items.map { item ->
                        FeedItemUi(
                            id = item.id,
                            title = item.title,
                            creatorName = item.creatorName,
                            creatorId = item.creatorId,
                            thumbnailUrl = item.thumbnailUrl,
                            categoryName = item.categoryName,
                            difficulty = item.difficulty,
                            hasQuiz = item.hasQuiz,
<<<<<<< Updated upstream
                            hasEli5 = item.hasEli5
                        )
                    }
=======
                            eli5VideoId = item.eli5VideoId
                        )
                    }
                    // If topic filter is set, filter items client-side by matching
                    // category name or derived hashtags against the filter
                    val items = if (topicFilter != null) {
                        allItems.filter { item ->
                            val hashtags = deriveHashtags(item)
                            item.categoryName.equals(topicFilter, ignoreCase = true) ||
                                hashtags.any { it.equals(topicFilter, ignoreCase = true) }
                        }
                    } else {
                        allItems
                    }
                    // Build pager pages: video → quiz card (if video has quiz)
                    val pages = buildList {
                        for (item in items) {
                            add(FeedPageItem.Video(item))
                            if (item.hasQuiz) {
                                add(FeedPageItem.QuizCard(
                                    videoId = item.id,
                                    creatorName = item.creatorName,
                                    categoryName = item.categoryName
                                ))
                            }
                        }
                    }
>>>>>>> Stashed changes
                    _state.value = _state.value.copy(
                        items = items,
                        pages = pages,
                        isLoading = false,
                        error = null,
                        hasMore = result.data.hasMore
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error: ${result.throwable.message}")
                }
            }
        }
    }

<<<<<<< Updated upstream
    fun refresh() {
        _state.value = _state.value.copy(page = 1)
        loadFeed()
=======
    fun setCurrentIndex(index: Int) {
        val current = _state.value
        if (index in current.pages.indices && index != current.currentIndex) {
            _state.value = current.copy(currentIndex = index)
        }
    }

    fun swipeNext() {
        val current = _state.value
        if (current.currentIndex < current.items.size - 1) {
            _state.value = current.copy(currentIndex = current.currentIndex + 1)
        }
    }

    fun swipePrevious() {
        val current = _state.value
        if (current.currentIndex > 0) {
            _state.value = current.copy(currentIndex = current.currentIndex - 1)
        }
>>>>>>> Stashed changes
    }
}
