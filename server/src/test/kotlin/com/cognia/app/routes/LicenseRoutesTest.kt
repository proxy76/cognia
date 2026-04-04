package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.moderation.*
import com.cognia.app.plugins.*
import com.cognia.app.repository.*
import com.cognia.app.service.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class LicenseRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-license-test-", ".db")
        testDbFile.deleteOnExit()

        val testModule = module {
            single { testConfig }
            single { UserRepository() }
            single { RefreshTokenRepository() }
            single { AuthService(get(), get(), get()) }
            single { UserProfileRepository() }
            single { UserProfileService(get()) }
            single { CategoryRepository() }
            single { CategoryService(get()) }
            single { VideoRepository() }
            single { VideoService(get()) }
            single { QuizRepository() }
            single { QuizAttemptRepository() }
            single { QuizService(get()) }
            single { ScoringService(get(), get()) }
            single { BadgeRepository() }
            single { LevelService() }
            single { BadgeService(get()) }
            single { FeedRepository() }
            single { FeedService(get(), get()) }
            single { TrackingService() }
            single { SearchService() }
            single { FollowRepository() }
            single { FollowService(get()) }
            single { FriendshipRepository() }
            single { FriendshipService(get()) }
            single { ChatRepository() }
            single { ChatService(get(), get()) }
            single { NotificationRepository() }
            single { NotificationService(get()) }
            single { AnalyticsService() }
            single<GoogleTokenVerifier> { DevGoogleTokenVerifier() }
            single<AppleTokenVerifier> { DevAppleTokenVerifier() }
            single { OAuthService(get(), get()) }
            single<RecommendationClient> { AnthropicRecommendationClient(get()) }
            single { OnboardingService(get()) }
            single { PreferenceRepository() }
            single { PreferenceService(get(), get()) }
            single<VideoProcessingService> { FfmpegVideoProcessingService(get()) }
            single { VideoProcessingQueue(get()) }
            single { ModerationRepository() }
            single { ReportRepository() }
            single { StrikeRepository() }
            single { LicenseRepository() }
            single { ModerationService(get(), get()) }
            single { ReportService(get(), get()) }
            single { StrikeService(get(), get()) }
            single { LicenseService(get(), get()) }
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
        email: String = "creator@example.com",
        password: String = "password123",
        displayName: String = "Test Creator"
    ): Pair<String, String> {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, displayName))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val auth = response.body<AuthResponse>()
        return Pair(auth.token, auth.userId)
    }

    private suspend fun setRoleAndGetToken(
        client: io.ktor.client.HttpClient,
        userId: String,
        role: String,
        email: String = "creator@example.com",
        password: String = "password123"
    ): String {
        transaction {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.role] = role
            }
        }
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val auth = loginResponse.body<AuthResponse>()
        return auth.token
    }

    // ── Request License Tests ──────────────────────────────────────

    @Test
    fun `POST request creates license request for REGULAR_CREATOR`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val response = client.post("/api/v1/license/request") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val result = response.body<LicenseRequestResponse>()
        assertEquals(userId, result.creatorId)
        assertEquals("PENDING", result.status)
    }

    @Test
    fun `POST request returns 403 for LEARNER`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.post("/api/v1/license/request") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    fun `POST request returns 403 when in cooldown`() = testApp { client ->
        val (_, creatorId) = registerAndGetToken(client)
        val creatorToken = setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        // Register a moderator and issue a strike
        val (_, modId) = registerAndGetToken(client, email = "mod@example.com", displayName = "Mod")
        setRoleAndGetToken(client, modId, "MODERATOR", email = "mod@example.com")

        val strikeRepo = StrikeRepository()
        val licenseRepo = LicenseRepository()
        val strikeService = StrikeService(strikeRepo, licenseRepo)
        strikeService.issueStrike(creatorId, modId, "Test strike")

        // Re-login to get fresh token
        val freshToken = setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val response = client.post("/api/v1/license/request") {
            header(HttpHeaders.Authorization, "Bearer $freshToken")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    // ── Approve License Tests ──────────────────────────────────────

    @Test
    fun `POST approve changes user role to LICENSED_CREATOR`() = testApp { client ->
        val (_, creatorId) = registerAndGetToken(client)
        setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val (_, modId) = registerAndGetToken(client, email = "mod@example.com", displayName = "Mod")
        val modToken = setRoleAndGetToken(client, modId, "MODERATOR", email = "mod@example.com")

        // Create license request
        val licenseRepo = LicenseRepository()
        val request = licenseRepo.createRequest(creatorId)

        val response = client.post("/api/v1/license/${request.id}/approve") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<LicenseRequestResponse>()
        assertEquals("APPROVED", result.status)

        // Verify user role was updated
        val user = transaction {
            UsersTable.selectAll().where { UsersTable.id eq creatorId }.singleOrNull()
        }
        assertNotNull(user)
        assertEquals("LICENSED_CREATOR", user[UsersTable.role])
    }

    // ── Reject License Tests ───────────────────────────────────────

    @Test
    fun `POST reject updates request status`() = testApp { client ->
        val (_, creatorId) = registerAndGetToken(client)
        setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val (_, modId) = registerAndGetToken(client, email = "mod@example.com", displayName = "Mod")
        val modToken = setRoleAndGetToken(client, modId, "MODERATOR", email = "mod@example.com")

        val licenseRepo = LicenseRepository()
        val request = licenseRepo.createRequest(creatorId)

        val response = client.post("/api/v1/license/${request.id}/reject") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
            contentType(ContentType.Application.Json)
            setBody(LicenseRejectRequest(reason = "Not enough content"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<LicenseRequestResponse>()
        assertEquals("REJECTED", result.status)
        assertEquals("Not enough content", result.rejectionReason)
    }

    // ── List Requests Tests ────────────────────────────────────────

    @Test
    fun `GET requests returns list for moderator`() = testApp { client ->
        val (_, creatorId) = registerAndGetToken(client)
        setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val (_, modId) = registerAndGetToken(client, email = "mod@example.com", displayName = "Mod")
        val modToken = setRoleAndGetToken(client, modId, "MODERATOR", email = "mod@example.com")

        val licenseRepo = LicenseRepository()
        licenseRepo.createRequest(creatorId)

        val response = client.get("/api/v1/license/requests") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<LicenseRequestListResponse>()
        assertEquals(1, result.items.size)
    }

    @Test
    fun `GET requests returns 403 for non-moderator`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/license/requests") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
