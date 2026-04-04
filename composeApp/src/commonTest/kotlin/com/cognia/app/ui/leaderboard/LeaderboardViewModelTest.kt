package com.cognia.app.ui.leaderboard

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LeaderboardViewModelTest {

    @Test
    fun initialStateIsLoadedWithEmptyEntries() {
        val viewModel = LeaderboardViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun retryReloadsData() {
        val viewModel = LeaderboardViewModel()
        viewModel.retry()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.entries.isEmpty())
    }

    @Test
    fun loadLeaderboardCompletesWithoutError() {
        val viewModel = LeaderboardViewModel()
        viewModel.loadLeaderboard()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
    }
}
