package com.cognia.app.ui.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatListViewModelTest {

    @Test
    fun initialStateLoadsAndHasEmptyConversations() {
        val viewModel = ChatListViewModel()
        val state = viewModel.state.value

        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.conversations.isEmpty())
    }

    @Test
    fun refreshTriggersReload() {
        val viewModel = ChatListViewModel()

        // After init, data is loaded
        assertFalse(viewModel.state.value.isLoading)

        // Refresh should reload and end up with same state
        viewModel.refresh()
        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.conversations.isEmpty())
    }
}
