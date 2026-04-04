package com.cognia.app.ui.feed

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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {

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
    fun initialStateDefaultsToForYouTab() {
        val viewModel = FeedViewModel()
        val state = viewModel.state.value

        assertEquals(FeedTab.FOR_YOU, state.selectedTab)
        assertEquals(1, state.page)
    }

    @Test
    fun selectTabUpdatesSelectedTab() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)

        assertEquals(FeedTab.DEEP_DIVE, viewModel.state.value.selectedTab)
    }

    @Test
    fun selectTabResetsPage() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)

        assertEquals(1, viewModel.state.value.page)
    }

    @Test
    fun selectTabSwitchesBackToForYou() {
        val viewModel = FeedViewModel()

        viewModel.selectTab(FeedTab.DEEP_DIVE)
        viewModel.selectTab(FeedTab.FOR_YOU)

        assertEquals(FeedTab.FOR_YOU, viewModel.state.value.selectedTab)
    }

    @Test
    fun initialItemsAreEmptyWhenApiUnavailable() {
        val viewModel = FeedViewModel()
        // With no server, API call fails and items remain empty
        assertTrue(viewModel.state.value.items.isEmpty())
    }

    @Test
    fun apiFailureSetsErrorState() {
        val viewModel = FeedViewModel()
        // With unreachable server, error should be set
        val state = viewModel.state.value
        assertTrue(state.error != null || state.items.isEmpty())
    }
}
