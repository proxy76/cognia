package com.cognia.app.ui.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileViewModelTest {

    @Test
    fun afterLoadHasProfileData() {
        val viewModel = ProfileViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Cognia User", state.displayName)
        assertEquals("LEARNER", state.role)
        assertEquals(1, state.level)
        assertEquals(0, state.totalPoints)
        assertEquals(0, state.badgeCount)
        assertEquals(0, state.followerCount)
        assertEquals(0, state.followingCount)
        assertEquals(0, state.friendCount)
    }

    @Test
    fun retryTriggersReload() {
        val viewModel = ProfileViewModel()

        // After init, data is loaded
        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Cognia User", viewModel.state.value.displayName)

        // Retry should reload and end up with same mock data
        viewModel.retry()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertEquals("Cognia User", state.displayName)
    }
}
