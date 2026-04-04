package com.cognia.app.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class SearchTab { ALL, VIDEOS, QUIZZES, CREATORS }

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: String
)

data class SearchUiState(
    val query: String = "",
    val selectedTab: SearchTab = SearchTab.ALL,
    val results: List<SearchResultItem> = emptyList(),
    val recentSearches: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SearchViewModel : ViewModel() {
    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadRecentSearches()
    }

    fun updateQuery(query: String) {
        _state.value = _state.value.copy(query = query)
        if (query.isNotBlank()) {
            performSearch()
        } else {
            _state.value = _state.value.copy(results = emptyList())
        }
    }

    fun selectTab(tab: SearchTab) {
        _state.value = _state.value.copy(selectedTab = tab)
        if (_state.value.query.isNotBlank()) {
            performSearch()
        }
    }

    fun clearQuery() {
        _state.value = _state.value.copy(query = "", results = emptyList())
    }

    private fun loadRecentSearches() {
        viewModelScope.launch {
            when (val result = api.getSearchHistory()) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(recentSearches = result.data.recentSearches)
                }
                is ApiResult.Error, is ApiResult.NetworkError -> {
                    // Silent fail for recent searches - not critical
                    _state.value = _state.value.copy(recentSearches = emptyList())
                }
            }
        }
    }

    private fun performSearch() {
        val query = _state.value.query
        val tab = _state.value.selectedTab
        _state.value = _state.value.copy(isLoading = true)

        val typeFilter = when (tab) {
            SearchTab.ALL -> null
            SearchTab.VIDEOS -> "video"
            SearchTab.QUIZZES -> "quiz"
            SearchTab.CREATORS -> "creator"
        }

        viewModelScope.launch {
            when (val result = api.search(query, typeFilter)) {
                is ApiResult.Success -> {
                    val items = mutableListOf<SearchResultItem>()
                    val groups = result.data.results

                    for (video in groups.videos) {
                        items.add(SearchResultItem(
                            id = video.id,
                            title = video.title,
                            subtitle = "${video.creator.displayName} - ${video.category.name}",
                            type = "video"
                        ))
                    }
                    for (quiz in groups.quizzes) {
                        items.add(SearchResultItem(
                            id = quiz.id,
                            title = quiz.title,
                            subtitle = "${quiz.quizType} - ${quiz.category.name}",
                            type = "quiz"
                        ))
                    }
                    for (creator in groups.creators) {
                        items.add(SearchResultItem(
                            id = creator.id,
                            title = creator.displayName,
                            subtitle = "Creator - ${creator.followerCount} followers",
                            type = "creator"
                        ))
                    }

                    _state.value = _state.value.copy(results = items, isLoading = false, error = null)
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
}
