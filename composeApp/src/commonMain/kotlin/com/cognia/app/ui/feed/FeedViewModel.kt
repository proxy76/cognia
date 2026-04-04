package com.cognia.app.ui.feed

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    init {
        loadMockData()
    }

    fun selectTab(tab: FeedTab) {
        _state.value = _state.value.copy(selectedTab = tab, page = 1)
        loadMockData()
    }

    fun loadMockData() {
        val tab = _state.value.selectedTab
        val items = when (tab) {
            FeedTab.FOR_YOU -> listOf(
                FeedItemUi("v1", "Intro to Quantum Physics", "Dr. Sarah", "u1", null, "Science", "MEDIUM", true),
                FeedItemUi("v2", "History of Rome", "HistoryBuff", "u2", null, "History", "EASY", false),
                FeedItemUi("v3", "Learn Guitar Basics", "MusicMaster", "u3", null, "Music", "EASY", true),
                FeedItemUi("v4", "Python for Beginners", "CodeAcademy", "u4", null, "Technology", "EASY", true),
                FeedItemUi("v5", "Abstract Art Explained", "ArtLover", "u5", null, "Art", "MEDIUM", false),
                FeedItemUi("v6", "Cooking Italian Pasta", "ChefMario", "u6", null, "Cooking", null, false)
            )
            FeedTab.DEEP_DIVE -> listOf(
                FeedItemUi("v7", "Advanced Calculus", "MathPro", "u7", null, "Mathematics", "HARD", true),
                FeedItemUi("v8", "Deep Learning Explained", "AIResearcher", "u8", null, "Technology", "HARD", true),
                FeedItemUi("v9", "Organic Chemistry", "ChemWiz", "u9", null, "Science", "HARD", false),
                FeedItemUi("v10", "Philosophy of Mind", "ThinkDeep", "u10", null, "Philosophy", "MEDIUM", true)
            )
        }
        _state.value = _state.value.copy(items = items, isLoading = false, error = null)
    }

    fun refresh() {
        _state.value = _state.value.copy(isLoading = true)
        // TODO: Wire to API
        loadMockData()
    }
}
