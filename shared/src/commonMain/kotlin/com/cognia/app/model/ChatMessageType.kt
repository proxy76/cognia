package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatMessageType {
    TEXT,
    SHARED_POST
}
