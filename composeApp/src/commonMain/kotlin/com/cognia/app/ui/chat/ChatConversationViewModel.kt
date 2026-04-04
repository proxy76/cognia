package com.cognia.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client
    private var currentUserId: String? = null

    fun loadConversation(conversationId: String) {
        _state.value = _state.value.copy(
            isLoading = true,
            error = null,
            conversationId = conversationId
        )

        viewModelScope.launch {
            // Get current user ID for isMe checks
            if (currentUserId == null) {
                when (val profileResult = api.getMyProfile()) {
                    is ApiResult.Success -> currentUserId = profileResult.data.id
                    else -> {}
                }
            }

            when (val result = api.getMessages(conversationId)) {
                is ApiResult.Success -> {
                    val messages = result.data.messages.map { msg ->
                        ChatMessageUi(
                            id = msg.id,
                            senderId = msg.senderId,
                            isMe = msg.senderId == currentUserId,
                            messageType = msg.messageType,
                            textContent = msg.textContent,
                            sharedContentId = msg.sharedContentId,
                            sharedContentType = msg.sharedContentType,
                            createdAt = msg.createdAt
                        )
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        participantName = "Chat",
                        messages = messages
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, messages = emptyList())
                }
            }
        }
    }

    fun updateMessageText(text: String) {
        _state.value = _state.value.copy(messageText = text)
    }

    fun sendMessage() {
        val text = _state.value.messageText.trim()
        if (text.isBlank()) return
        val conversationId = _state.value.conversationId

        _state.value = _state.value.copy(isSending = true)

        viewModelScope.launch {
            when (val result = api.sendTextMessage(conversationId, text)) {
                is ApiResult.Success -> {
                    val msg = result.data
                    val newMessage = ChatMessageUi(
                        id = msg.id,
                        senderId = msg.senderId,
                        isMe = true,
                        messageType = msg.messageType,
                        textContent = msg.textContent,
                        createdAt = msg.createdAt
                    )
                    _state.value = _state.value.copy(
                        isSending = false,
                        messageText = "",
                        messages = _state.value.messages + newMessage
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isSending = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isSending = false, error = "Failed to send message")
                }
            }
        }
    }

    fun sendSharedContent(contentId: String, contentType: String) {
        val conversationId = _state.value.conversationId
        _state.value = _state.value.copy(isSending = true)

        viewModelScope.launch {
            when (val result = api.sendSharedPost(conversationId, contentId, contentType)) {
                is ApiResult.Success -> {
                    val msg = result.data
                    val newMessage = ChatMessageUi(
                        id = msg.id,
                        senderId = msg.senderId,
                        isMe = true,
                        messageType = msg.messageType,
                        sharedContentId = msg.sharedContentId,
                        sharedContentType = msg.sharedContentType,
                        createdAt = msg.createdAt
                    )
                    _state.value = _state.value.copy(
                        isSending = false,
                        messages = _state.value.messages + newMessage
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isSending = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isSending = false, error = "Failed to send")
                }
            }
        }
    }

    fun loadMore() {
        // TODO: Pagination with cursor from API response
    }
}
