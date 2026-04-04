package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.common.StatusResponse
import com.cognia.app.dto.notification.NotificationListResponse
import com.cognia.app.dto.notification.UnreadCountResponse
import com.cognia.app.plugins.*
import com.cognia.app.repository.NotificationRepository
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.AuthService
import com.cognia.app.service.NotificationService
import com.cognia.app.service.UserProfileService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient, NotificationService) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-notification-test-", ".db")
        testDbFile.deleteOnExit()

        val notificationRepository = NotificationRepository()
        val notificationService = NotificationService(notificationRepository)

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
            single { notificationRepository }
            single { notificationService }
        }

        install(Koin) {
            modules(testModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)
            configureSerialization()
            configureStatusPages()
            configureAuth()
            configureWebSockets()
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = false
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }

        block(client, notificationService)
    }

    private suspend fun registerAndGetToken(
        client: io.ktor.client.HttpClient,
        email: String = "notif@example.com",
        password: String = "password123",
        displayName: String = "Notification User"
    ): Pair<String, String> {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, displayName))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val auth = response.body<AuthResponse>()
        return Pair(auth.token, auth.userId)
    }

    @Test
    fun `GET notifications returns empty list initially`() = testApp { client, _ ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertTrue(body.notifications.isEmpty())
        assertEquals(0, body.unreadCount)
    }

    @Test
    fun `GET notifications returns created notifications`() = testApp { client, notificationService ->
        val (token, userId) = registerAndGetToken(client)

        notificationService.createNotification(userId, "FOLLOW", "New follower", "Someone followed you")
        notificationService.createNotification(userId, "BADGE", "Badge earned", "You earned a badge!")

        val response = client.get("/api/v1/notifications") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertEquals(2, body.notifications.size)
        assertEquals(2, body.unreadCount)
    }

    @Test
    fun `GET unread-count returns correct count`() = testApp { client, notificationService ->
        val (token, userId) = registerAndGetToken(client)

        notificationService.createNotification(userId, "FOLLOW", "Follower 1", "User A followed you")
        notificationService.createNotification(userId, "FOLLOW", "Follower 2", "User B followed you")
        notificationService.createNotification(userId, "BADGE", "Badge", "New badge!")

        val response = client.get("/api/v1/notifications/unread-count") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<UnreadCountResponse>()
        assertEquals(3, body.unreadCount)
    }

    @Test
    fun `POST mark as read marks single notification`() = testApp { client, notificationService ->
        val (token, userId) = registerAndGetToken(client)

        val notification = notificationService.createNotification(userId, "FOLLOW", "New follower", "Someone followed you")

        val response = client.post("/api/v1/notifications/${notification.id}/read") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        // Verify unread count went down
        val countResponse = client.get("/api/v1/notifications/unread-count") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = countResponse.body<UnreadCountResponse>()
        assertEquals(0, body.unreadCount)
    }

    @Test
    fun `POST mark as read returns 404 for unknown notification`() = testApp { client, _ ->
        val (token, _) = registerAndGetToken(client)

        val response = client.post("/api/v1/notifications/nonexistent-id/read") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST read-all marks all notifications as read`() = testApp { client, notificationService ->
        val (token, userId) = registerAndGetToken(client)

        notificationService.createNotification(userId, "FOLLOW", "Follower 1", "User A followed you")
        notificationService.createNotification(userId, "BADGE", "Badge", "New badge!")

        val response = client.post("/api/v1/notifications/read-all") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)

        val countResponse = client.get("/api/v1/notifications/unread-count") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        val body = countResponse.body<UnreadCountResponse>()
        assertEquals(0, body.unreadCount)
    }

    @Test
    fun `GET notifications without auth returns 401`() = testApp { client, _ ->
        val response = client.get("/api/v1/notifications")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET notifications supports pagination`() = testApp { client, notificationService ->
        val (token, userId) = registerAndGetToken(client)

        repeat(5) { i ->
            notificationService.createNotification(userId, "INFO", "Notif $i", "Body $i")
        }

        val response = client.get("/api/v1/notifications?page=1&limit=2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<NotificationListResponse>()
        assertEquals(2, body.notifications.size)
        assertEquals(5, body.totalCount)
        assertEquals(1, body.page)
        assertEquals(2, body.limit)
    }
}
