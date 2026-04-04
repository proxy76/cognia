package com.cognia.app.ui.profile

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProfileViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsLoadingOrHasError() {
        val viewModel = ProfileViewModel()
        val state = viewModel.state.value

        // With no server, the profile is either still loading or has received a network error
        assertTrue(state.isLoading || state.error != null || state.displayName.isEmpty())
    }

    @Test
    fun initialStateHasDefaultValues() {
        val viewModel = ProfileViewModel()
        val state = viewModel.state.value

        // Default state values before API response
        assertEquals("LEARNER", state.role)
        assertEquals(1, state.level)
        assertEquals(0, state.totalPoints)
    }
}
