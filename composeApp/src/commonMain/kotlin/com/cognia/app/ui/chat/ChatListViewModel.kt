package com.cognia.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    init {
        loadConversations()
    }

    fun loadConversations() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getConversations()) {
                is ApiResult.Success -> {
                    val conversations = result.data.conversations.map { conv ->
                        ConversationUi(
                            id = conv.id,
                            participantId = conv.participant.id,
                            participantName = conv.participant.displayName,
                            participantAvatarUrl = conv.participant.avatarUrl,
                            lastMessageText = conv.lastMessage?.text,
                            lastMessageAt = conv.lastMessage?.createdAt
                        )
                    }
                    _state.value = _state.value.copy(isLoading = false, conversations = conversations)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, conversations = emptyList())
                }
            }
        }
    }

    fun refresh() {
        loadConversations()
    }
}
