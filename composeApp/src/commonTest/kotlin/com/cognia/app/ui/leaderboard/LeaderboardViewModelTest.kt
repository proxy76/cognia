package com.cognia.app.ui.leaderboard

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LeaderboardViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsLoadingOrEmpty() {
        val viewModel = LeaderboardViewModel()
        val state = viewModel.state.value

        // With no server, state is either still loading or has empty entries
        assertTrue(state.isLoading || state.entries.isEmpty())
    }

    @Test
    fun initialEntriesAreEmpty() {
        val viewModel = LeaderboardViewModel()
        assertTrue(viewModel.state.value.entries.isEmpty())
    }
}
