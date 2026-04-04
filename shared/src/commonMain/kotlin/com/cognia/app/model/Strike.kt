package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Strike(
    val id: String,
    val userId: String,
    val moderatorId: String,
    val reason: String,
    val cooldownUntil: Instant,
    val createdAt: Instant
)
