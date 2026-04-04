package com.cognia.app.ui.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChatListUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val conversations: List<ConversationUi> = emptyList()
)

data class ConversationUi(
    val id: String,
    val participantId: String,
    val participantName: String,
    val participantAvatarUrl: String? = null,
    val lastMessageText: String? = null,
    val lastMessageAt: String? = null
)

class ChatListViewModel : ViewModel() {
    private val _state = MutableStateFlow(ChatListUiState())
    val state: StateFlow<ChatListUiState> = _state.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/chat/conversations
        // For now, show empty list (no mock data)
        _state.value = _state.value.copy(isLoading = false, conversations = emptyList())
    }

    fun refresh() {
        loadConversations()
    }
}
