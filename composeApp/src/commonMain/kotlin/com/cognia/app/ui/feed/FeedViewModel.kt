package com.cognia.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeedItemUi(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val thumbnailUrl: String?,
    val categoryName: String,
    val difficulty: String?,
    val hasQuiz: Boolean
)

data class FeedUiState(
    val items: List<FeedItemUi> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val topicFilter: String? = null,
)

class FeedViewModel : ViewModel() {
    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadFeed()
    }

    fun loadFeed(topicFilter: String? = null) {
        _state.value = _state.value.copy(isLoading = true, error = null, topicFilter = topicFilter, currentIndex = 0)

        viewModelScope.launch {
            when (val result = api.getForYouFeed(page = 1, limit = 40)) {
                is ApiResult.Success -> {
                    val allItems = result.data.items.map { item ->
                        FeedItemUi(
                            id = item.id,
                            title = item.title,
                            creatorName = item.creatorName,
                            creatorId = item.creatorId,
                            thumbnailUrl = item.thumbnailUrl,
                            categoryName = item.categoryName,
                            difficulty = item.difficulty,
                            hasQuiz = item.hasQuiz
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
                    _state.value = _state.value.copy(
                        items = items,
                        isLoading = false,
                        error = null,
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

    fun setCurrentIndex(index: Int) {
        val current = _state.value
        if (index in current.items.indices && index != current.currentIndex) {
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
    }
}

/** Derive hashtag-style tags from a feed item's title and category. */
fun deriveHashtags(item: FeedItemUi): List<String> {
    val titleWords = item.title
        .split(" ", "-", ":", ",")
        .filter { it.length > 3 }
        .map { it.lowercase().trim() }
        .distinct()
    val tags = (listOf(item.categoryName.lowercase()) + titleWords).distinct().take(4)
    return if (tags.size >= 2) tags else listOf(item.categoryName.lowercase(), "education", "learning", "cognia")
}
