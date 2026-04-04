package com.cognia.app.ui.search

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchViewModelTest {

    @Test
    fun initialStateHasEmptyQuery() {
        val viewModel = SearchViewModel()
        val state = viewModel.state.value

        assertEquals("", state.query)
        assertEquals(SearchTab.ALL, state.selectedTab)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun initialStateLoadsRecentSearches() {
        val viewModel = SearchViewModel()
        val state = viewModel.state.value

        assertTrue(state.recentSearches.isNotEmpty())
        assertTrue(state.recentSearches.contains("quantum physics"))
    }

    @Test
    fun updateQuerySetsQueryAndFilters() {
        val viewModel = SearchViewModel()

        viewModel.updateQuery("quantum")

        val state = viewModel.state.value
        assertEquals("quantum", state.query)
        assertTrue(state.results.isNotEmpty())
        // All results should match "quantum"
        assertTrue(state.results.all {
            it.title.lowercase().contains("quantum") || it.subtitle.lowercase().contains("quantum")
        })
    }

    @Test
    fun clearQueryResetsResults() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("quantum")

        viewModel.clearQuery()

        val state = viewModel.state.value
        assertEquals("", state.query)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun selectTabFiltersResults() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("a") // broad query to get multiple results

        viewModel.selectTab(SearchTab.VIDEOS)
        val videoResults = viewModel.state.value.results
        assertTrue(videoResults.all { it.type == "video" })

        viewModel.selectTab(SearchTab.QUIZZES)
        val quizResults = viewModel.state.value.results
        assertTrue(quizResults.all { it.type == "quiz" })

        viewModel.selectTab(SearchTab.CREATORS)
        val creatorResults = viewModel.state.value.results
        assertTrue(creatorResults.all { it.type == "creator" })
    }

    @Test
    fun selectAllTabShowsAllTypes() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("a") // broad query

        viewModel.selectTab(SearchTab.ALL)

        val types = viewModel.state.value.results.map { it.type }.toSet()
        // ALL tab should have at least 2 different types with a broad query
        assertTrue(types.size >= 2)
    }

    @Test
    fun emptyQueryShowsNoResults() {
        val viewModel = SearchViewModel()
        viewModel.updateQuery("quantum")
        assertTrue(viewModel.state.value.results.isNotEmpty())

        viewModel.updateQuery("")
        assertTrue(viewModel.state.value.results.isEmpty())
    }

    @Test
    fun noMatchingResultsReturnsEmptyList() {
        val viewModel = SearchViewModel()

        viewModel.updateQuery("xyznonexistent")

        assertTrue(viewModel.state.value.results.isEmpty())
    }
}
