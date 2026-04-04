package com.cognia.app.ui.reel

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReelViewModelTest {

    @Test
    fun initialStateLoadsMockVideos() {
        val viewModel = ReelViewModel()
        val state = viewModel.state.value

        assertEquals(5, state.videos.size)
        assertEquals(0, state.currentIndex)
        assertTrue(state.isPlaying)
        assertFalse(state.isBuffering)
    }

    @Test
    fun swipeToNextIncrementsCurrentIndex() {
        val viewModel = ReelViewModel()

        viewModel.swipeToNext()

        assertEquals(1, viewModel.state.value.currentIndex)
    }

    @Test
    fun swipeToPreviousDecrementsCurrentIndex() {
        val viewModel = ReelViewModel()
        viewModel.swipeToNext() // go to 1
        viewModel.swipeToNext() // go to 2

        viewModel.swipeToPrevious()

        assertEquals(1, viewModel.state.value.currentIndex)
    }

    @Test
    fun cannotSwipePastLastVideo() {
        val viewModel = ReelViewModel()
        val lastIndex = viewModel.state.value.videos.size - 1

        // Swipe to the end
        repeat(lastIndex + 5) { viewModel.swipeToNext() }

        assertEquals(lastIndex, viewModel.state.value.currentIndex)
    }

    @Test
    fun cannotSwipeBeforeFirstVideo() {
        val viewModel = ReelViewModel()

        viewModel.swipeToPrevious()

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
    fun videosHaveCorrectQuizFlags() {
        val viewModel = ReelViewModel()
        val videos = viewModel.state.value.videos

        assertTrue(videos[0].hasQuiz)  // Quantum Physics
        assertFalse(videos[1].hasQuiz) // History of Rome
        assertTrue(videos[2].hasQuiz)  // Guitar Basics
        assertTrue(videos[3].hasQuiz)  // Python
        assertFalse(videos[4].hasQuiz) // Abstract Art
    }

    @Test
    fun loadVideosResetsState() {
        val viewModel = ReelViewModel()
        viewModel.swipeToNext()
        viewModel.swipeToNext()
        viewModel.togglePlayPause()

        viewModel.loadVideos()

        val state = viewModel.state.value
        assertEquals(0, state.currentIndex)
        assertTrue(state.isPlaying)
        assertEquals(5, state.videos.size)
    }
}
