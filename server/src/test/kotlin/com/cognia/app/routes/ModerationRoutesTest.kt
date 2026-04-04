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
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ModerationRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-moderation-test-", ".db")
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

            transaction {
                CategoriesTable.insert {
                    it[id] = TEST_CATEGORY_ID
                    it[name] = "Test Category"
                    it[slug] = "test-category"
                }
            }

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
        email: String = "user@example.com",
        password: String = "password123",
        displayName: String = "Test User"
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
        email: String = "user@example.com",
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

    // ── Queue Tests ────────────────────────────────────────────────

    @Test
    fun `GET queue returns pending reviews for moderator`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val modToken = setRoleAndGetToken(client, userId, "MODERATOR")

        // Seed a moderation review
        transaction {
            ModerationReviewsTable.insert {
                it[id] = "review-1"
                it[contentId] = "video-1"
                it[contentType] = "VIDEO"
                it[isPostPublication] = 0
                it[createdAt] = "2024-01-01T00:00:00"
            }
            VideosTable.insert {
                it[id] = "video-1"
                it[creatorId] = userId
                it[title] = "Test Video"
                it[categoryId] = TEST_CATEGORY_ID
                it[status] = "PENDING_REVIEW"
                it[createdAt] = "2024-01-01T00:00:00"
                it[updatedAt] = "2024-01-01T00:00:00"
            }
        }

        val response = client.get("/api/v1/moderation/queue") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val queue = response.body<ModerationQueueResponse>()
        assertTrue(queue.items.isNotEmpty())
        assertEquals("review-1", queue.items[0].reviewId)
        assertEquals("Test Video", queue.items[0].title)
    }

    @Test
    fun `GET queue returns 403 for non-moderator`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/moderation/queue") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    // ── Decide Tests ───────────────────────────────────────────────

    @Test
    fun `POST decide approves content and transitions to PUBLISHED`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val modToken = setRoleAndGetToken(client, userId, "MODERATOR")

        // Seed video and review
        transaction {
            VideosTable.insert {
                it[id] = "video-2"
                it[creatorId] = userId
                it[title] = "Approve Me"
                it[categoryId] = TEST_CATEGORY_ID
                it[status] = "PENDING_REVIEW"
                it[createdAt] = "2024-01-01T00:00:00"
                it[updatedAt] = "2024-01-01T00:00:00"
            }
            ModerationReviewsTable.insert {
                it[id] = "review-2"
                it[contentId] = "video-2"
                it[contentType] = "VIDEO"
                it[isPostPublication] = 0
                it[createdAt] = "2024-01-01T00:00:00"
            }
        }

        val response = client.post("/api/v1/moderation/review-2/decide") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
            contentType(ContentType.Application.Json)
            setBody(ModerationDecisionRequest(decision = "APPROVED", reason = "Looks good"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<ModerationReviewResponse>()
        assertEquals("APPROVED", result.decision)
        assertNotNull(result.decidedAt)

        // Verify video was transitioned to PUBLISHED
        val video = transaction {
            VideosTable.selectAll().where { VideosTable.id eq "video-2" }.singleOrNull()
        }
        assertNotNull(video)
        assertEquals("PUBLISHED", video[VideosTable.status])
    }

    @Test
    fun `POST decide rejects content`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val modToken = setRoleAndGetToken(client, userId, "MODERATOR")

        transaction {
            VideosTable.insert {
                it[id] = "video-3"
                it[creatorId] = userId
                it[title] = "Reject Me"
                it[categoryId] = TEST_CATEGORY_ID
                it[status] = "PENDING_REVIEW"
                it[createdAt] = "2024-01-01T00:00:00"
                it[updatedAt] = "2024-01-01T00:00:00"
            }
            ModerationReviewsTable.insert {
                it[id] = "review-3"
                it[contentId] = "video-3"
                it[contentType] = "VIDEO"
                it[isPostPublication] = 0
                it[createdAt] = "2024-01-01T00:00:00"
            }
        }

        val response = client.post("/api/v1/moderation/review-3/decide") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
            contentType(ContentType.Application.Json)
            setBody(ModerationDecisionRequest(decision = "REJECTED", reason = "Violates policy"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<ModerationReviewResponse>()
        assertEquals("REJECTED", result.decision)

        val video = transaction {
            VideosTable.selectAll().where { VideosTable.id eq "video-3" }.singleOrNull()
        }
        assertNotNull(video)
        assertEquals("REJECTED", video[VideosTable.status])
    }

    // ── Review History Tests ───────────────────────────────────────

    @Test
    fun `GET reviews returns history for moderator`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val modToken = setRoleAndGetToken(client, userId, "MODERATOR")

        transaction {
            ModerationReviewsTable.insert {
                it[id] = "review-hist-1"
                it[contentId] = "video-h1"
                it[contentType] = "VIDEO"
                it[decision] = "APPROVED"
                it[moderatorId] = userId
                it[isPostPublication] = 0
                it[createdAt] = "2024-01-01T00:00:00"
                it[decidedAt] = "2024-01-02T00:00:00"
            }
        }

        val response = client.get("/api/v1/moderation/reviews") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val reviews = response.body<ModerationReviewListResponse>()
        assertTrue(reviews.items.isNotEmpty())
    }

    // ── Report Tests ───────────────────────────────────────────────

    @Test
    fun `POST report creates report and moderation review`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        // Need a creator for the video
        val (_, creatorId) = registerAndGetToken(client, email = "creator@example.com", displayName = "Creator")
        val creatorToken = setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR", email = "creator@example.com")

        transaction {
            VideosTable.insert {
                it[id] = "video-report"
                it[this.creatorId] = creatorId
                it[title] = "Reported Video"
                it[categoryId] = TEST_CATEGORY_ID
                it[status] = "PUBLISHED"
                it[createdAt] = "2024-01-01T00:00:00"
                it[updatedAt] = "2024-01-01T00:00:00"
            }
        }

        // Any authenticated user can report
        val userToken = setRoleAndGetToken(client, userId, "LEARNER")

        val response = client.post("/api/v1/reports") {
            header(HttpHeaders.Authorization, "Bearer $userToken")
            contentType(ContentType.Application.Json)
            setBody(ReportCreateRequest(
                contentId = "video-report",
                contentType = "VIDEO",
                reason = "Inappropriate content"
            ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val report = response.body<ReportResponse>()
        assertEquals("PENDING", report.status)
        assertEquals("Inappropriate content", report.reason)
    }

    @Test
    fun `GET reports returns list for moderator`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val modToken = setRoleAndGetToken(client, userId, "MODERATOR")

        transaction {
            ContentReportsTable.insert {
                it[id] = "report-1"
                it[reporterId] = userId
                it[contentId] = "video-x"
                it[contentType] = "VIDEO"
                it[reason] = "Spam"
                it[status] = "PENDING"
                it[createdAt] = "2024-01-01T00:00:00"
            }
        }

        val response = client.get("/api/v1/reports") {
            header(HttpHeaders.Authorization, "Bearer $modToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val reports = response.body<ReportListResponse>()
        assertTrue(reports.items.isNotEmpty())
    }

    @Test
    fun `GET reports returns 403 for non-moderator`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/reports") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    companion object {
        private const val TEST_CATEGORY_ID = "test-cat-mod"
    }
}
