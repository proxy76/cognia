package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.analytics.CreatorAnalyticsResponse
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.plugins.*
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.repository.NotificationRepository
import com.cognia.app.service.AnalyticsService
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
import kotlin.test.assertTrue

class AnalyticsRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-analytics-test-", ".db")
        testDbFile.deleteOnExit()

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
            single { AnalyticsService() }
            single { NotificationRepository() }
            single { NotificationService(get()) }
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

        block(client)
    }

    private suspend fun registerAndGetToken(
        client: io.ktor.client.HttpClient,
        email: String = "analytics@example.com",
        password: String = "password123",
        displayName: String = "Analytics User"
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
    fun `GET creator analytics returns empty stats for new user`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/analytics/creator") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<CreatorAnalyticsResponse>()
        assertEquals(0L, body.totalViews)
        assertEquals(0L, body.totalQuizAttempts)
        assertEquals(0.0, body.averageQuizScore)
        assertTrue(body.videoStats.isEmpty())
    }

    @Test
    fun `GET creator analytics without auth returns 401`() = testApp { client ->
        val response = client.get("/api/v1/analytics/creator")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
