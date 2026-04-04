package com.cognia.app.ui.feed

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class FeedViewModelTest {

    @Test
    fun initialStateLoadsForYouMockData() {
        val viewModel = FeedViewModel()
        val state = viewModel.state.value

        assertEquals(FeedTab.FOR_YOU, state.selectedTab)
        assertEquals(6, state.items.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun selectTabSwitchesToDeepDive() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)

        val state = viewModel.state.value
        assertEquals(FeedTab.DEEP_DIVE, state.selectedTab)
        assertEquals(4, state.items.size)
    }

    @Test
    fun selectTabSwitchesBackToForYou() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)
        viewModel.selectTab(FeedTab.FOR_YOU)

        val state = viewModel.state.value
        assertEquals(FeedTab.FOR_YOU, state.selectedTab)
        assertEquals(6, state.items.size)
    }

    @Test
    fun forYouItemsContainExpectedCategories() {
        val viewModel = FeedViewModel()
        val categories = viewModel.state.value.items.map { it.categoryName }

        assertEquals(true, categories.contains("Science"))
        assertEquals(true, categories.contains("History"))
        assertEquals(true, categories.contains("Music"))
        assertEquals(true, categories.contains("Technology"))
    }

    @Test
    fun deepDiveItemsContainHarderContent() {
        val viewModel = FeedViewModel()
        viewModel.selectTab(FeedTab.DEEP_DIVE)

        val difficulties = viewModel.state.value.items.mapNotNull { it.difficulty }
        assertEquals(true, difficulties.contains("HARD"))
    }

    @Test
    fun feedItemsHaveCorrectQuizFlags() {
        val viewModel = FeedViewModel()
        val items = viewModel.state.value.items

        // First item: "Intro to Quantum Physics" has quiz
        assertEquals(true, items[0].hasQuiz)
        // Second item: "History of Rome" does not have quiz
        assertEquals(false, items[1].hasQuiz)
    }

    @Test
    fun refreshResetsState() {
        val viewModel = FeedViewModel()
        viewModel.selectTab(FeedTab.DEEP_DIVE)

        viewModel.selectTab(FeedTab.FOR_YOU)
        viewModel.refresh()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(6, state.items.size)
    }

    @Test
    fun tabSwitchResetsPage() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)

        assertEquals(1, viewModel.state.value.page)
    }
}
