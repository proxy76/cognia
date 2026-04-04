package com.cognia.app.service

import com.cognia.app.dto.notification.NotificationListResponse
import com.cognia.app.dto.notification.NotificationResponse
import com.cognia.app.repository.NotificationRepository
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import com.cognia.app.dto.notification.WebSocketNotificationEvent

class NotificationService(
    private val notificationRepository: NotificationRepository
) {
    // Connected WebSocket sessions keyed by userId
    private val connections = mutableMapOf<String, MutableSet<WebSocketSession>>()
    private val mutex = Mutex()

    fun createNotification(
        userId: String,
        type: String,
        title: String,
        body: String,
        referenceId: String? = null,
        referenceType: String? = null
    ): NotificationResponse {
        val row = notificationRepository.create(userId, type, title, body, referenceId, referenceType)
        return row.toResponse()
    }

    fun getNotifications(userId: String, page: Int, limit: Int): NotificationListResponse {
        val notifications = notificationRepository.findByUserId(userId, page, limit)
        val totalCount = notificationRepository.countByUserId(userId)
        val unreadCount = notificationRepository.getUnreadCount(userId)
        return NotificationListResponse(
            notifications = notifications.map { it.toResponse() },
            page = page,
            limit = limit,
            totalCount = totalCount,
            unreadCount = unreadCount
        )
    }

    fun markAsRead(notificationId: String, userId: String): Boolean {
        return notificationRepository.markAsRead(notificationId, userId)
    }

    fun markAllAsRead(userId: String): Int {
        return notificationRepository.markAllAsRead(userId)
    }

    fun getUnreadCount(userId: String): Int {
        return notificationRepository.getUnreadCount(userId)
    }

    suspend fun addConnection(userId: String, session: WebSocketSession) {
        mutex.withLock {
            connections.getOrPut(userId) { mutableSetOf() }.add(session)
        }
    }

    suspend fun removeConnection(userId: String, session: WebSocketSession) {
        mutex.withLock {
            connections[userId]?.remove(session)
            if (connections[userId]?.isEmpty() == true) {
                connections.remove(userId)
            }
        }
    }

    suspend fun pushNotification(userId: String, notification: NotificationResponse) {
        val sessions = mutex.withLock { connections[userId]?.toSet() } ?: return
        val event = WebSocketNotificationEvent(notification = notification)
        val json = Json.encodeToString(event)
        for (session in sessions) {
            try {
                session.send(Frame.Text(json))
            } catch (_: Exception) {
                // Session may have been closed
            }
        }
    }

    private fun NotificationRepository.NotificationRow.toResponse() = NotificationResponse(
        id = id,
        type = type,
        title = title,
        body = body,
        referenceId = referenceId,
        referenceType = referenceType,
        read = read,
        createdAt = createdAt
    )
}
