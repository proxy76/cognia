package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ModerationDecision {
    APPROVED,
    REJECTED
}
