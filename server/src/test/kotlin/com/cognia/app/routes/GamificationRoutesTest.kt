package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.gamification.BadgeListResponse
import com.cognia.app.dto.gamification.LeaderboardResponse
import com.cognia.app.plugins.*
import com.cognia.app.repository.BadgeRepository
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.AuthService
import com.cognia.app.service.BadgeService
import com.cognia.app.service.LevelService
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
import kotlin.test.assertTrue

class GamificationRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-gamification-test-", ".db")
        testDbFile.deleteOnExit()

        val badgeRepository = BadgeRepository()

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
            single { badgeRepository }
            single { LevelService() }
            single { BadgeService(get()) }
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

            // Seed badges for testing
            val badgeService = BadgeService(badgeRepository)
            badgeService.seedBadges()
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
        email: String = "gamification@example.com",
        password: String = "password123",
        displayName: String = "Gamification User"
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
    fun `GET leaderboard returns ranked users`() = testApp { client ->
        val (token1, _) = registerAndGetToken(client, email = "user1@example.com", displayName = "User One")
        val (token2, _) = registerAndGetToken(client, email = "user2@example.com", displayName = "User Two")

        val response = client.get("/api/v1/leaderboard") {
            header(HttpHeaders.Authorization, "Bearer $token1")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val leaderboard = response.body<LeaderboardResponse>()
        assertTrue(leaderboard.entries.isNotEmpty())
        // All users start at 0 points, so they should all be rank-ordered
        assertEquals(1, leaderboard.entries.first().rank)
    }

    @Test
    fun `GET leaderboard respects limit parameter`() = testApp { client ->
        val (token, _) = registerAndGetToken(client, email = "user1@example.com")
        registerAndGetToken(client, email = "user2@example.com")
        registerAndGetToken(client, email = "user3@example.com")

        val response = client.get("/api/v1/leaderboard?limit=2") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val leaderboard = response.body<LeaderboardResponse>()
        assertEquals(2, leaderboard.entries.size)
    }

    @Test
    fun `GET leaderboard without auth returns 401`() = testApp { client ->
        val response = client.get("/api/v1/leaderboard")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `GET user badges returns awarded badges`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        // Award a badge directly via the repository
        val badgeRepository = BadgeRepository()
        badgeRepository.awardBadge(userId, "FIRST_QUIZ")

        val response = client.get("/api/v1/users/me/badges") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val badgesResponse = response.body<BadgeListResponse>()
        assertTrue(badgesResponse.badges.isNotEmpty())
        assertEquals("FIRST_QUIZ", badgesResponse.badges.first().id)
        assertEquals("Quiz Beginner", badgesResponse.badges.first().name)
    }

    @Test
    fun `GET user badges returns empty when no badges earned`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/users/me/badges") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val badgesResponse = response.body<BadgeListResponse>()
        assertTrue(badgesResponse.badges.isEmpty())
    }

    @Test
    fun `GET all badges returns seeded badge definitions`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/badges") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val badgesResponse = response.body<BadgeListResponse>()
        assertEquals(7, badgesResponse.badges.size)
    }

    @Test
    fun `GET user badges without auth returns 401`() = testApp { client ->
        val response = client.get("/api/v1/users/me/badges")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }
}
