package com.cognia.app.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cognia.app.config.AppConfig
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.RefreshTokenRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.plugins.configureAuth
import com.cognia.app.plugins.configureSerialization
import com.cognia.app.plugins.configureStatusPages
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.routes.authRoutes
import com.cognia.app.service.AuthService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.application.install
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotEquals

class AuthMiddlewareTest {

    private val testConfig = AppConfig.fromEnvironment()
    private val jwtSecret = testConfig.jwt.secret
    private val jwtIssuer = testConfig.jwt.issuer

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-auth-test-", ".db")
        testDbFile.deleteOnExit()

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
        }

        install(Koin) {
            modules(testModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            configureSerialization()
            configureStatusPages()
            configureAuth()

            routing {
                authRoutes()

                // A protected route accessible to any authenticated user
                authenticate("auth-jwt") {
                    get("/api/v1/protected") {
                        val principal = call.principal<UserPrincipal>()!!
                        call.respondText("Hello ${principal.userId}")
                    }

                    // A route restricted to CREATOR roles
                    authorize("REGULAR_CREATOR", "LICENSED_CREATOR") {
                        get("/api/v1/creator-only") {
                            call.respondText("Creator content")
                        }
                    }

                    // A route restricted to MODERATOR role
                    authorize("MODERATOR") {
                        get("/api/v1/moderator-only") {
                            call.respondText("Moderator content")
                        }
                    }
                }
            }
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

    private fun createValidJwt(
        userId: String = "test-user-id",
        email: String = "test@example.com",
        role: String = "LEARNER",
        expiresAt: Date = Date(System.currentTimeMillis() + 3_600_000)
    ): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withSubject(userId)
            .withClaim("email", email)
            .withClaim("role", role)
            .withExpiresAt(expiresAt)
            .sign(Algorithm.HMAC256(jwtSecret))
    }

    // --- JWT Authentication Tests ---

    @Test
    fun `valid JWT passes authentication and request proceeds`() = testApp { client ->
        val token = createValidJwt()
        val response = client.get("/api/v1/protected") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `expired JWT returns 401`() = testApp { client ->
        val expiredToken = createValidJwt(
            expiresAt = Date(System.currentTimeMillis() - 10_000)
        )
        val response = client.get("/api/v1/protected") {
            header(HttpHeaders.Authorization, "Bearer $expiredToken")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `malformed JWT returns 401`() = testApp { client ->
        val response = client.get("/api/v1/protected") {
            header(HttpHeaders.Authorization, "Bearer not-a-valid-jwt")
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `missing Authorization header returns 401`() = testApp { client ->
        val response = client.get("/api/v1/protected")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    // --- Refresh Token Tests ---

    @Test
    fun `valid refresh token returns new token pair`() = testApp { client ->
        // Register a user to get a refresh token
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("refresh@example.com", "password123", "Refresh User"))
        }
        assertEquals(HttpStatusCode.Created, registerResponse.status)
        val authResponse = registerResponse.body<AuthResponse>()

        // Use refresh token
        val refreshResponse = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(authResponse.refreshToken))
        }
        assertEquals(HttpStatusCode.OK, refreshResponse.status)

        val newAuth = refreshResponse.body<AuthResponse>()
        assertNotNull(newAuth.token)
        assertNotNull(newAuth.refreshToken)
        // Access tokens may be identical if generated in the same second (same claims + exp)
        // but refresh tokens must always differ since they are random UUIDs
        assertNotEquals(authResponse.refreshToken, newAuth.refreshToken)
    }

    @Test
    fun `invalid refresh token returns 401`() = testApp { client ->
        val response = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest("invalid-refresh-token"))
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `refresh with used (rotated) token returns 401`() = testApp { client ->
        // Register to get a refresh token
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest("rotate@example.com", "password123", "Rotate User"))
        }
        val authResponse = registerResponse.body<AuthResponse>()
        val originalRefreshToken = authResponse.refreshToken

        // First refresh - should succeed
        val firstRefresh = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(originalRefreshToken))
        }
        assertEquals(HttpStatusCode.OK, firstRefresh.status)

        // Second refresh with same token - should fail (token was rotated/deleted)
        val secondRefresh = client.post("/api/v1/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(RefreshTokenRequest(originalRefreshToken))
        }
        assertEquals(HttpStatusCode.Unauthorized, secondRefresh.status)
    }

    // --- Role-Based Authorization Tests ---

    @Test
    fun `LEARNER accessing CREATOR-only route returns 403`() = testApp { client ->
        val token = createValidJwt(role = "LEARNER")
        val response = client.get("/api/v1/creator-only") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `REGULAR_CREATOR accessing CREATOR route returns 200`() = testApp { client ->
        val token = createValidJwt(role = "REGULAR_CREATOR")
        val response = client.get("/api/v1/creator-only") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `LICENSED_CREATOR accessing CREATOR route returns 200`() = testApp { client ->
        val token = createValidJwt(role = "LICENSED_CREATOR")
        val response = client.get("/api/v1/creator-only") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `ADMIN accessing any role-gated route returns 200`() = testApp { client ->
        val token = createValidJwt(role = "ADMIN")

        // ADMIN accessing creator-only route
        val creatorResponse = client.get("/api/v1/creator-only") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, creatorResponse.status)

        // ADMIN accessing moderator-only route
        val moderatorResponse = client.get("/api/v1/moderator-only") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, moderatorResponse.status)
    }

    @Test
    fun `unauthenticated request to role-gated route returns 401`() = testApp { client ->
        val response = client.get("/api/v1/creator-only")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
