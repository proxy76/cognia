package com.cognia.app.ui.search

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
        // TODO: Wire to API
        _state.value = _state.value.copy(
            recentSearches = listOf("quantum physics", "python", "guitar", "history")
        )
    }

    private fun performSearch() {
        // TODO: Wire to API
        val query = _state.value.query.lowercase()
        val tab = _state.value.selectedTab

        val allResults = listOf(
            SearchResultItem("v1", "Intro to Quantum Physics", "Dr. Sarah - Science", "video"),
            SearchResultItem("v2", "History of Rome", "HistoryBuff - History", "video"),
            SearchResultItem("v3", "Python for Beginners", "CodeAcademy - Technology", "video"),
            SearchResultItem("q1", "Quantum Physics Quiz", "Multiple Choice - Science", "quiz"),
            SearchResultItem("q2", "Python Basics Quiz", "Multiple Choice - Technology", "quiz"),
            SearchResultItem("c1", "Dr. Sarah", "Creator - 1.2K followers", "creator"),
            SearchResultItem("c2", "CodeAcademy", "Creator - 5K followers", "creator")
        )

        val filtered = allResults.filter { item ->
            val matchesQuery = item.title.lowercase().contains(query) ||
                item.subtitle.lowercase().contains(query)
            val matchesTab = when (tab) {
                SearchTab.ALL -> true
                SearchTab.VIDEOS -> item.type == "video"
                SearchTab.QUIZZES -> item.type == "quiz"
                SearchTab.CREATORS -> item.type == "creator"
            }
            matchesQuery && matchesTab
        }

        _state.value = _state.value.copy(results = filtered, isLoading = false)
    }
}
