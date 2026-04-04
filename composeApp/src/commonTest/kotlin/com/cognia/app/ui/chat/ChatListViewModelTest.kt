package com.cognia.app.ui.chat

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatListViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsLoadingOrEmpty() {
        val viewModel = ChatListViewModel()
        val state = viewModel.state.value

        // With no server, state is either loading (coroutine not yet resolved)
        // or has empty conversations (network error handled)
        assertTrue(state.isLoading || state.conversations.isEmpty())
    }

    @Test
    fun initialConversationsAreEmpty() {
        val viewModel = ChatListViewModel()
        // Before API responds, conversations list should be empty
        assertTrue(viewModel.state.value.conversations.isEmpty())
    }
}
