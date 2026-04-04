package com.cognia.app.ui.notification

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsLoadingOrEmpty() {
        val viewModel = NotificationViewModel()
        val state = viewModel.state.value

        // With no server, state is either still loading or has empty notifications
        assertTrue(state.isLoading || state.notifications.isEmpty())
    }

    @Test
    fun initialNotificationsAreEmpty() {
        val viewModel = NotificationViewModel()
        assertTrue(viewModel.state.value.notifications.isEmpty())
    }

    @Test
    fun initialUnreadCountIsZero() {
        val viewModel = NotificationViewModel()
        assertEquals(0, viewModel.state.value.unreadCount)
    }

    @Test
    fun initialCurrentPageIsOne() {
        val viewModel = NotificationViewModel()
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun markAsReadOnEmptyStateDoesNotCrash() {
        val viewModel = NotificationViewModel()
        viewModel.markAsRead("nonexistent")
        assertEquals(0, viewModel.state.value.unreadCount)
    }

    @Test
    fun markAllAsReadOnEmptyStateWorks() {
        val viewModel = NotificationViewModel()
        viewModel.markAllAsRead()

        val state = viewModel.state.value
        assertEquals(0, state.unreadCount)
        assertTrue(state.notifications.all { it.read })
    }
}
