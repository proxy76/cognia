package com.cognia.app.ui.notification

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationItem(
    val id: String,
    val type: String,
    val title: String,
    val body: String,
    val referenceId: String? = null,
    val referenceType: String? = null,
    val read: Boolean = false,
    val createdAt: String
)

data class NotificationUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val currentPage: Int = 1,
    val hasMore: Boolean = false
)

class NotificationViewModel : ViewModel() {
    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _state.value = _state.value.copy(isLoading = true, error = null)
        // TODO: Wire to actual API call GET /api/v1/notifications
        // For now, show empty state
        _state.value = NotificationUiState(
            isLoading = false,
            notifications = emptyList(),
            unreadCount = 0,
            currentPage = 1,
            hasMore = false
        )
    }

    fun refresh() {
        _state.value = _state.value.copy(currentPage = 1)
        loadNotifications()
    }

    fun markAsRead(notificationId: String) {
        // TODO: Wire to actual API call POST /api/v1/notifications/{id}/read
        val updated = _state.value.notifications.map {
            if (it.id == notificationId && !it.read) it.copy(read = true) else it
        }
        val newUnread = updated.count { !it.read }
        _state.value = _state.value.copy(notifications = updated, unreadCount = newUnread)
    }

    fun markAllAsRead() {
        // TODO: Wire to actual API call POST /api/v1/notifications/read-all
        val updated = _state.value.notifications.map { it.copy(read = true) }
        _state.value = _state.value.copy(notifications = updated, unreadCount = 0)
    }
}
