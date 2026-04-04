package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.plugins.*
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AuthRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-test-", ".db")
        testDbFile.deleteOnExit()

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            val testModule = module {
                single { testConfig }
                single { UserRepository() }
                single { RefreshTokenRepository() }
                single { AuthService(get(), get(), get()) }
                single<GoogleTokenVerifier> { DevGoogleTokenVerifier() }
                single<AppleTokenVerifier> { DevAppleTokenVerifier() }
                single { OAuthService(get(), get()) }
                single { UserProfileRepository() }
                single { UserProfileService(get()) }
            }

            install(Koin) {
                modules(testModule)
            }

            configureSerialization()
            configureStatusPages()
            configureAuth()
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

    @Test
    fun `POST register with valid data returns 201`() = testApp { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("test@example.com", "password123", "Test User"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<AuthResponse>()
        assertNotNull(body.userId)
        assertNotNull(body.token)
        assertNotNull(body.refreshToken)
        assertTrue(body.token.isNotBlank())
    }

    @Test
    fun `POST register with duplicate email returns 409`() = testApp { client ->
        // First registration
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("dup@example.com", "password123", "User One"))
        }

        // Duplicate registration
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("dup@example.com", "password456", "User Two"))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `POST login with valid credentials returns 200`() = testApp { client ->
        // Register first
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("login@example.com", "password123", "Login User"))
        }

        // Login
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("login@example.com", "password123"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertNotNull(body.userId)
        assertNotNull(body.token)
        assertNotNull(body.refreshToken)
    }

    @Test
    fun `POST login with wrong password returns 401`() = testApp { client ->
        // Register first
        client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("wrong@example.com", "password123", "Wrong Pass User"))
        }

        // Login with wrong password
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("wrong@example.com", "wrongpassword"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST login with unknown email returns 401`() = testApp { client ->
        val response = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest("nonexistent@example.com", "password123"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST register with short password returns 400`() = testApp { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("short@example.com", "short", "Short Pass"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST register with invalid email returns 400`() = testApp { client ->
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("notanemail", "password123", "Invalid Email"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
