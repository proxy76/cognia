package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.content.VideoDetailResponse
import com.cognia.app.dto.content.VideoUpdateRequest
import com.cognia.app.dto.content.VideoUploadResponse
import com.cognia.app.plugins.*
import com.cognia.app.repository.CategoryRepository
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.repository.VideoRepository
import com.cognia.app.service.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.insert
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class VideoRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-video-test-", ".db")
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
        }

        install(Koin) {
            modules(testModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            // Seed a test category
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
     * Register a user and return token + userId.
     */
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

    /**
     * Set a user's role in the database and return a fresh JWT with the new role.
     */
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
        // Re-login to get a token with updated role
        val loginResponse = client.post("/api/v1/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("email" to email, "password" to password))
        }
        assertEquals(HttpStatusCode.OK, loginResponse.status)
        val auth = loginResponse.body<AuthResponse>()
        return auth.token
    }

    private fun createMultipartVideoData(
        title: String = "Test Video",
        description: String? = "A test video",
        categoryId: String = TEST_CATEGORY_ID,
        fileContent: ByteArray = "fake video content".toByteArray(),
        fileName: String = "test.mp4"
    ) = formData {
        append("title", title)
        if (description != null) append("description", description)
        append("categoryId", categoryId)
        append("file", fileContent, Headers.build {
            append(HttpHeaders.ContentType, "video/mp4")
            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
        })
    }

    // ── Upload Tests ────────────────────────────────────────────────

    @Test
    fun `POST upload creates video with DRAFT status`() = testApp { client ->
        val (initialToken, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val response = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val body = response.body<VideoUploadResponse>()
        assertEquals("DRAFT", body.status)
        assertNotNull(body.id)
    }

    // ── Get Video Tests ─────────────────────────────────────────────

    @Test
    fun `GET video returns details`() = testApp { client ->
        val (initialToken, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        // Create a video first
        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Get the video
        val response = client.get("/api/v1/videos/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val video = response.body<VideoDetailResponse>()
        assertEquals(created.id, video.id)
        assertEquals("Test Video", video.title)
        assertEquals("A test video", video.description)
        assertEquals(TEST_CATEGORY_ID, video.categoryId)
        assertEquals("DRAFT", video.status)
    }

    @Test
    fun `GET nonexistent video returns 404`() = testApp { client ->
        val (initialToken, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val response = client.get("/api/v1/videos/nonexistent-id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    // ── Update Metadata Tests ───────────────────────────────────────

    @Test
    fun `PUT update metadata succeeds for owner`() = testApp { client ->
        val (initialToken, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        // Create a video
        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Update metadata
        val response = client.put("/api/v1/videos/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(VideoUpdateRequest(title = "Updated Title"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val video = response.body<VideoDetailResponse>()
        assertEquals("Updated Title", video.title)
    }

    @Test
    fun `PUT update metadata rejected for non-owner`() = testApp { client ->
        // Register creator and create a video
        val (_, creatorId) = registerAndGetToken(client)
        val creatorToken = setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $creatorToken")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Register another user
        val (_, otherId) = registerAndGetToken(client, email = "other@example.com", displayName = "Other User")
        val otherToken = setRoleAndGetToken(client, otherId, "REGULAR_CREATOR", email = "other@example.com")

        // Try to update as non-owner
        val response = client.put("/api/v1/videos/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $otherToken")
            contentType(ContentType.Application.Json)
            setBody(VideoUpdateRequest(title = "Hacked Title"))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    // ── Submit for Review Tests ─────────────────────────────────────

    @Test
    fun `POST submit transitions to PENDING_REVIEW`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        // Create a video
        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Submit for review
        val response = client.post("/api/v1/videos/${created.id}/submit") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val video = response.body<VideoDetailResponse>()
        assertEquals("PENDING_REVIEW", video.status)
    }

    // ── Publish Tests ───────────────────────────────────────────────

    @Test
    fun `POST publish by licensed creator succeeds`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "LICENSED_CREATOR")

        // Create a video
        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Publish directly
        val response = client.post("/api/v1/videos/${created.id}/publish") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val video = response.body<VideoDetailResponse>()
        assertEquals("PUBLISHED", video.status)
        assertNotNull(video.publishedAt)
    }

    @Test
    fun `POST publish by regular creator returns 403`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        // Create a video
        val createResponse = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }
        val created = createResponse.body<VideoUploadResponse>()

        // Try to publish directly
        val response = client.post("/api/v1/videos/${created.id}/publish") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    // ── Auth Tests ──────────────────────────────────────────────────

    @Test
    fun `POST upload without auth returns 401`() = testApp { client ->
        val response = client.post("/api/v1/videos") {
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST upload as learner returns 403`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)
        // User is LEARNER by default

        val response = client.post("/api/v1/videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
            setBody(MultiPartFormDataContent(createMultipartVideoData()))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    companion object {
        private const val TEST_CATEGORY_ID = "test-cat-001"
    }
}
