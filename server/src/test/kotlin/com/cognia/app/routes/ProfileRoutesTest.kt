package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.profile.PublicProfileResponse
import com.cognia.app.dto.profile.UpdateProfileRequest
import com.cognia.app.dto.profile.UserProfileResponse
import com.cognia.app.plugins.*
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.AuthService
import com.cognia.app.service.UserProfileService
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-profile-test-", ".db")
        testDbFile.deleteOnExit()

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
        }

        install(Koin) {
            modules(testModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

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

    /**
     * Helper: register a user and return their auth token.
     */
    private suspend fun registerAndGetToken(
        client: io.ktor.client.HttpClient,
        email: String = "profile@example.com",
        password: String = "password123",
        displayName: String = "Profile User"
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
    fun `GET me returns full profile data`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        val response = client.get("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val profile = response.body<UserProfileResponse>()
        assertEquals(userId, profile.id)
        assertEquals("profile@example.com", profile.email)
        assertEquals("Profile User", profile.displayName)
        assertEquals("LEARNER", profile.role)
        assertEquals(1, profile.level)
        assertEquals(0, profile.totalPoints)
        assertEquals(0, profile.badgeCount)
        assertEquals(0, profile.followerCount)
        assertEquals(0, profile.followingCount)
        assertEquals(0, profile.friendCount)
    }

    @Test
    fun `PUT me updates displayName`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val updateResponse = client.put("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(displayName = "Updated Name"))
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val profile = updateResponse.body<UserProfileResponse>()
        assertEquals("Updated Name", profile.displayName)
    }

    @Test
    fun `PUT me update ignores role and email fields`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        // UpdateProfileRequest only has displayName, avatarUrl, selfDescription
        // so role/email cannot be changed through this endpoint
        val updateResponse = client.put("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(displayName = "New Name"))
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val profile = updateResponse.body<UserProfileResponse>()
        assertEquals("profile@example.com", profile.email)
        assertEquals("LEARNER", profile.role)
        assertEquals("New Name", profile.displayName)
    }

    @Test
    fun `GET userId returns public profile with limited fields`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        val response = client.get("/api/v1/users/$userId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val profile = response.body<PublicProfileResponse>()
        assertEquals(userId, profile.id)
        assertEquals("Profile User", profile.displayName)
        assertEquals("LEARNER", profile.role)
        assertEquals(1, profile.level)
        assertEquals(0, profile.followerCount)
        assertEquals(0, profile.videoCount)
        assertTrue(profile.badges.isEmpty())
    }

    @Test
    fun `GET unknown userId returns 404`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/users/nonexistent-user-id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET me without auth returns 401`() = testApp { client ->
        val response = client.get("/api/v1/users/me")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT me updates selfDescription`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val updateResponse = client.put("/api/v1/users/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(UpdateProfileRequest(selfDescription = "I love learning!"))
        }

        assertEquals(HttpStatusCode.OK, updateResponse.status)
        val profile = updateResponse.body<UserProfileResponse>()
        assertEquals("I love learning!", profile.selfDescription)
    }
}
