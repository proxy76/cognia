package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ChatConversation(
    val id: String,
    val participantA: String,
    val participantB: String,
    val createdAt: Instant,
    val lastMessageAt: Instant? = null
)
