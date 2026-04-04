package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.dto.auth.AppleOAuthRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.GoogleOAuthRequest
import com.cognia.app.plugins.configureAuth
import com.cognia.app.plugins.configureRouting
import com.cognia.app.plugins.configureSerialization
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.*
import io.ktor.client.call.*
import io.ktor.server.application.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.*

class OAuthRoutesTest {

    // -- Mock verifiers for testing --

    class MockGoogleTokenVerifier(
        private val result: GoogleTokenPayload? = null
    ) : GoogleTokenVerifier {
        override fun verify(idToken: String): GoogleTokenPayload? = result
    }

    class MockAppleTokenVerifier(
        private val result: AppleTokenPayload? = null
    ) : AppleTokenVerifier {
        override fun verify(identityToken: String, authorizationCode: String): AppleTokenPayload? = result
    }

    private val testConfig = AppConfig.fromEnvironment()

    private fun oauthTestApp(
        googleVerifier: GoogleTokenVerifier = MockGoogleTokenVerifier(),
        appleVerifier: AppleTokenVerifier = MockAppleTokenVerifier(),
        block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit
    ) = testApplication {
        val testDbFile = File.createTempFile("cognia-oauth-test-", ".db")
        testDbFile.deleteOnExit()

        val koinModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single<GoogleTokenVerifier> { googleVerifier }
            single<AppleTokenVerifier> { appleVerifier }
            single { OAuthService(get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
        }

        install(Koin) {
            modules(koinModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)
            configureSerialization()
            configureAuth()
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        block(client)
    }

    // ── Google OAuth Tests ──────────────────────────────────────────────

    @Test
    fun `google auth with valid token returns 200 with AuthResponse`() = oauthTestApp(
        googleVerifier = MockGoogleTokenVerifier(
            GoogleTokenPayload(email = "user@gmail.com", name = "Test User", emailVerified = true)
        )
    ) { client ->
        val response = client.post("/api/v1/auth/oauth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleOAuthRequest(idToken = "fake.token.here"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertTrue(body.userId.isNotBlank())
        assertTrue(body.token.isNotBlank())
        assertTrue(body.refreshToken.isNotBlank())
    }

    @Test
    fun `google auth with invalid token returns 401`() = oauthTestApp(
        googleVerifier = MockGoogleTokenVerifier(result = null)
    ) { client ->
        val response = client.post("/api/v1/auth/oauth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleOAuthRequest(idToken = "invalid-token"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `google auth creates new user if email not found`() = oauthTestApp(
        googleVerifier = MockGoogleTokenVerifier(
            GoogleTokenPayload(email = "newuser@gmail.com", name = "New User", emailVerified = true)
        )
    ) { client ->
        val response = client.post("/api/v1/auth/oauth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleOAuthRequest(idToken = "fake.token.here"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertTrue(body.userId.isNotBlank())
    }

    @Test
    fun `google auth logs in existing user if email found`() = oauthTestApp(
        googleVerifier = MockGoogleTokenVerifier(
            GoogleTokenPayload(email = "existing@gmail.com", name = "Existing User", emailVerified = true)
        )
    ) { client ->
        // First call creates the user
        val firstResponse = client.post("/api/v1/auth/oauth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleOAuthRequest(idToken = "fake.token.here"))
        }
        val firstBody = firstResponse.body<AuthResponse>()

        // Second call should log in the same user
        val secondResponse = client.post("/api/v1/auth/oauth/google") {
            contentType(ContentType.Application.Json)
            setBody(GoogleOAuthRequest(idToken = "fake.token.here"))
        }
        val secondBody = secondResponse.body<AuthResponse>()

        assertEquals(HttpStatusCode.OK, secondResponse.status)
        assertEquals(firstBody.userId, secondBody.userId)
    }

    // ── Apple OAuth Tests ───────────────────────────────────────────────

    @Test
    fun `apple auth with valid token returns 200 with AuthResponse`() = oauthTestApp(
        appleVerifier = MockAppleTokenVerifier(
            AppleTokenPayload(email = "user@icloud.com", name = "Apple User")
        )
    ) { client ->
        val response = client.post("/api/v1/auth/oauth/apple") {
            contentType(ContentType.Application.Json)
            setBody(AppleOAuthRequest(identityToken = "fake.token.here", authorizationCode = "auth-code"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.body<AuthResponse>()
        assertTrue(body.userId.isNotBlank())
        assertTrue(body.token.isNotBlank())
        assertTrue(body.refreshToken.isNotBlank())
    }

    @Test
    fun `apple auth with invalid token returns 401`() = oauthTestApp(
        appleVerifier = MockAppleTokenVerifier(result = null)
    ) { client ->
        val response = client.post("/api/v1/auth/oauth/apple") {
            contentType(ContentType.Application.Json)
            setBody(AppleOAuthRequest(identityToken = "invalid-token", authorizationCode = "auth-code"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
