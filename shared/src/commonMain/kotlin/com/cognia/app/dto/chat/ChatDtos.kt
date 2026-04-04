package com.cognia.app.dto.chat

import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class ConversationResponse(
    val id: String,
    val participant: UserSummary,
    val lastMessage: LastMessageSummary? = null,
    val unreadCount: Int = 0
)

@Serializable
data class LastMessageSummary(
    val text: String? = null,
    val createdAt: String
)

@Serializable
data class ConversationListResponse(
    val conversations: List<ConversationResponse>
)

@Serializable
data class ChatMessageResponse(
    val id: String,
    val senderId: String,
    val messageType: String,
    val textContent: String? = null,
    val sharedContentId: String? = null,
    val sharedContentType: String? = null,
    val createdAt: String
)

@Serializable
data class MessageListResponse(
    val messages: List<ChatMessageResponse>,
    val nextCursor: String? = null
)

@Serializable
data class SendTextMessageRequest(
    val messageType: String,
    val textContent: String
)

@Serializable
data class SendSharedPostRequest(
    val messageType: String,
    val sharedContentId: String,
    val sharedContentType: String
)

@Serializable
data class CreateConversationRequest(
    val participantId: String
)

@Serializable
data class WebSocketChatMessage(
    val conversationId: String,
    val messageType: String = "TEXT",
    val textContent: String? = null,
    val sharedContentId: String? = null,
    val sharedContentType: String? = null
)

@Serializable
data class WebSocketChatEvent(
    val type: String,
    val message: ChatMessageResponse? = null,
    val error: String? = null
)
