package com.cognia.app.ui.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    init {
        loadNotifications()
    }

    fun loadNotifications() {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getNotifications(_state.value.currentPage)) {
                is ApiResult.Success -> {
                    val notifications = result.data.notifications.map { n ->
                        NotificationItem(
                            id = n.id,
                            type = n.type,
                            title = n.title,
                            body = n.body,
                            referenceId = n.referenceId,
                            referenceType = n.referenceType,
                            read = n.read,
                            createdAt = n.createdAt
                        )
                    }
                    _state.value = NotificationUiState(
                        isLoading = false,
                        notifications = notifications,
                        unreadCount = result.data.unreadCount,
                        currentPage = result.data.page,
                        hasMore = notifications.size >= result.data.limit
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, notifications = emptyList())
                }
            }
        }
    }

    fun refresh() {
        _state.value = _state.value.copy(currentPage = 1)
        loadNotifications()
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            api.markNotificationRead(notificationId)
            val updated = _state.value.notifications.map {
                if (it.id == notificationId && !it.read) it.copy(read = true) else it
            }
            val newUnread = updated.count { !it.read }
            _state.value = _state.value.copy(notifications = updated, unreadCount = newUnread)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            api.markAllNotificationsRead()
            val updated = _state.value.notifications.map { it.copy(read = true) }
            _state.value = _state.value.copy(notifications = updated, unreadCount = 0)
        }
    }
}
