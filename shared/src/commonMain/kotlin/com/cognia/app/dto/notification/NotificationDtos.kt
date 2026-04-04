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
    val page: Int = 1,
    val limit: Int = 20,
    val totalCount: Int = 0,
    val unreadCount: Int = 0
)

@Serializable
data class UnreadCountResponse(
    val unreadCount: Int
)

@Serializable
data class WebSocketNotificationEvent(
    val type: String = "NEW_NOTIFICATION",
    val notification: NotificationResponse
)
