package com.cognia.app.dto.notification

import kotlinx.serialization.Serializable

@Serializable
data class NotificationResponse(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val read: Boolean = false,
    val createdAt: String
)

@Serializable
data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val unreadCount: Int = 0
)
