package com.cognia.app.ui.chat

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatConversationViewModelTest {

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
    fun loadConversationSetsConversationId() {
        val viewModel = ChatConversationViewModel()
        viewModel.loadConversation("conv-123")

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals("conv-123", state.conversationId)
        assertEquals("Chat", state.participantName)
    }

    @Test
    fun updateMessageTextUpdatesState() {
        val viewModel = ChatConversationViewModel()
        viewModel.updateMessageText("Hello")
        assertEquals("Hello", viewModel.state.value.messageText)
    }

    @Test
    fun sendMessageAddsToListAndClearsInput() {
        val viewModel = ChatConversationViewModel()
        viewModel.loadConversation("conv-123")
        viewModel.updateMessageText("Hello there!")
        viewModel.sendMessage()

        val state = viewModel.state.value
        assertEquals("", state.messageText)
        assertFalse(state.isSending)
        assertEquals(1, state.messages.size)
        assertEquals("Hello there!", state.messages[0].textContent)
        assertTrue(state.messages[0].isMe)
        assertEquals("TEXT", state.messages[0].messageType)
    }

    @Test
    fun sendEmptyMessageDoesNothing() {
        val viewModel = ChatConversationViewModel()
        viewModel.loadConversation("conv-123")
        viewModel.updateMessageText("   ")
        viewModel.sendMessage()

        assertTrue(viewModel.state.value.messages.isEmpty())
    }

    @Test
    fun sendSharedContentAddsSharedMessage() {
        val viewModel = ChatConversationViewModel()
        viewModel.loadConversation("conv-123")
        viewModel.sendSharedContent("video-456", "VIDEO")

        val state = viewModel.state.value
        assertEquals(1, state.messages.size)
        assertEquals("SHARED_POST", state.messages[0].messageType)
        assertEquals("video-456", state.messages[0].sharedContentId)
        assertEquals("VIDEO", state.messages[0].sharedContentType)
        assertTrue(state.messages[0].isMe)
    }

    @Test
    fun multipleMessagesMaintainOrder() {
        val viewModel = ChatConversationViewModel()
        viewModel.loadConversation("conv-123")

        viewModel.updateMessageText("First")
        viewModel.sendMessage()
        viewModel.updateMessageText("Second")
        viewModel.sendMessage()
        viewModel.updateMessageText("Third")
        viewModel.sendMessage()

        val messages = viewModel.state.value.messages
        assertEquals(3, messages.size)
        assertEquals("First", messages[0].textContent)
        assertEquals("Second", messages[1].textContent)
        assertEquals("Third", messages[2].textContent)
    }
}
