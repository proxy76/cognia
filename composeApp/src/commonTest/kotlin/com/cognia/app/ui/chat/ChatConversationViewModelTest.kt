package com.cognia.app.ui.chat

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ChatConversationViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsEmpty() {
        val viewModel = ChatConversationViewModel()
        val state = viewModel.state.value

        assertTrue(state.isLoading)
        assertEquals("", state.conversationId)
        assertEquals("", state.participantName)
        assertTrue(state.messages.isEmpty())
        assertEquals("", state.messageText)
        assertFalse(state.isSending)
    }

    @Test
    fun updateMessageTextUpdatesState() {
        val viewModel = ChatConversationViewModel()
        viewModel.updateMessageText("Hello")
        assertEquals("Hello", viewModel.state.value.messageText)
    }

    @Test
    fun sendEmptyMessageDoesNothing() {
        val viewModel = ChatConversationViewModel()
        viewModel.updateMessageText("   ")
        viewModel.sendMessage()

        // sendMessage trims blank text and returns early
        assertTrue(viewModel.state.value.messages.isEmpty())
        assertFalse(viewModel.state.value.isSending)
    }

    @Test
    fun updateMessageTextMultipleTimes() {
        val viewModel = ChatConversationViewModel()
        viewModel.updateMessageText("Hello")
        assertEquals("Hello", viewModel.state.value.messageText)

        viewModel.updateMessageText("World")
        assertEquals("World", viewModel.state.value.messageText)

        viewModel.updateMessageText("")
        assertEquals("", viewModel.state.value.messageText)
    }
}
