package com.cognia.app.ui.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatConversationUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val conversationId: String = "",
    val participantName: String = "",
    val messages: List<ChatMessageUi> = emptyList(),
    val messageText: String = "",
    val isSending: Boolean = false
)

data class ChatMessageUi(
    val id: String,
    val senderId: String,
    val isMe: Boolean,
    val messageType: String,
    val textContent: String? = null,
    val sharedContentId: String? = null,
    val sharedContentType: String? = null,
    val createdAt: String
)

class ChatConversationViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatConversationUiState())
    val state: StateFlow<ChatConversationUiState> = _state.asStateFlow()

    fun loadConversation(conversationId: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            error = null,
            conversationId = conversationId
        )
        // TODO: Wire to actual API call GET /api/v1/chat/conversations/{id}/messages
        _state.value = _state.value.copy(
            isLoading = false,
            participantName = "Chat",
            messages = emptyList()
        )
    }

    fun updateMessageText(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _state.value.messageText.trim()
        if (text.isBlank()) return

        _state.value = _state.value.copy(isSending = true)
        // TODO: Wire to actual API call POST /api/v1/chat/conversations/{id}/messages
        val newMessage = ChatMessageUi(
            id = "msg-${_state.value.messages.size}",
            senderId = "me",
            isMe = true,
            messageType = "TEXT",
            textContent = text,
            createdAt = "just now"
        )
        _state.value = _state.value.copy(
            isSending = false,
            messageText = "",
            messages = _state.value.messages + newMessage
        )
    }

    fun sendSharedContent(contentId: String, contentType: String) {
        _state.value = _state.value.copy(isSending = true)
        // TODO: Wire to actual API call POST /api/v1/chat/conversations/{id}/messages
        val newMessage = ChatMessageUi(
            id = "msg-${_state.value.messages.size}",
            senderId = "me",
            isMe = true,
            messageType = "SHARED_POST",
            sharedContentId = contentId,
            sharedContentType = contentType,
            createdAt = "just now"
        )
        _state.value = _state.value.copy(
            isSending = false,
            messages = _state.value.messages + newMessage
        )
    }

    fun loadMore() {
        // TODO: Pagination with cursor
    }
}
