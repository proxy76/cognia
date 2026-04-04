package com.cognia.app.ui.search

import com.cognia.app.network.ApiClientProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        ApiClientProvider.init("http://localhost:99999")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasEmptyQuery() {
        val viewModel = SearchViewModel()
        val state = viewModel.state.value

        assertEquals("", state.query)
        assertEquals(SearchTab.ALL, state.selectedTab)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun updateQuerySetsQuery() {
        val viewModel = SearchViewModel()

        viewModel.updateQuery("quantum")

        assertEquals("quantum", viewModel.state.value.query)
    }

    @Test
    fun clearQueryResetsQueryAndResults() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("quantum")

        viewModel.clearQuery()

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun updateQueryToEmptyClearsResults() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("quantum")

        viewModel.updateQuery("")

        assertTrue(viewModel.state.value.results.isEmpty())
    }

    @Test
    fun selectTabUpdatesSelectedTab() {
        val viewModel = SearchViewModel()

        viewModel.selectTab(SearchTab.VIDEOS)

        assertEquals(SearchTab.VIDEOS, viewModel.state.value.selectedTab)
    }

    @Test
    fun selectTabCyclesThroughAllTabs() {
        val viewModel = SearchViewModel()

        for (tab in SearchTab.entries) {
            viewModel.selectTab(tab)
            assertEquals(tab, viewModel.state.value.selectedTab)
        }
    }
}
