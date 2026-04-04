package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val body: String,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val read: Boolean = false,
    val createdAt: Instant
)
