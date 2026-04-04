package com.cognia.app.ui.reel

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
class ReelViewModelTest {

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
    fun initialStateHasDefaultValues() {
        val viewModel = ReelViewModel()
        val state = viewModel.state.value

        assertEquals(0, state.currentIndex)
        assertTrue(state.isPlaying)
        assertFalse(state.isBuffering)
    }

    @Test
    fun cannotSwipeBeforeFirstVideo() {
        val viewModel = ReelViewModel()

        viewModel.swipeToPrevious()

        assertEquals(0, viewModel.state.value.currentIndex)
    }

    @Test
    fun cannotSwipeNextWithNoVideos() {
        val viewModel = ReelViewModel()
        // No videos loaded (API fails), so swipeToNext should not change index
        viewModel.swipeToNext()

        assertEquals(0, viewModel.state.value.currentIndex)
    }

    @Test
    fun togglePlayPauseTogglesIsPlaying() {
        val viewModel = ReelViewModel()

        assertTrue(viewModel.state.value.isPlaying)

        viewModel.togglePlayPause()
        assertFalse(viewModel.state.value.isPlaying)

        viewModel.togglePlayPause()
        assertTrue(viewModel.state.value.isPlaying)
    }

    @Test
    fun onBufferingUpdatesIsBuffering() {
        val viewModel = ReelViewModel()

        assertFalse(viewModel.state.value.isBuffering)

        viewModel.onBuffering(true)
        assertTrue(viewModel.state.value.isBuffering)

        viewModel.onBuffering(false)
        assertFalse(viewModel.state.value.isBuffering)
    }

    @Test
    fun videosAreEmptyWhenApiUnavailable() {
        val viewModel = ReelViewModel()
        assertTrue(viewModel.state.value.videos.isEmpty())
    }
}
