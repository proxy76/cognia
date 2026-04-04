package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val messageType: ChatMessageType,
    val textContent: String? = null,
    val sharedContentId: String? = null,
    val sharedContentType: SharedContentType? = null,
    val createdAt: Instant
)
