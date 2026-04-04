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
    val hasQuiz: Boolean
)

data class FeedUiState(
    val selectedTab: FeedTab = FeedTab.FOR_YOU,
    val items: List<FeedItemUi> = emptyList(),
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
                            hasQuiz = item.hasQuiz
                        )
                    }
                    _state.value = _state.value.copy(
                        items = items,
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

    fun refresh() {
        _state.value = _state.value.copy(page = 1)
        loadFeed()
    }
}
