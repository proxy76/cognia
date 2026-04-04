package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.plugins.*
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VideoStreamRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(
        setupData: (() -> Unit)? = null,
        block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit
    ) = testApplication {
        val testDbFile = File.createTempFile("cognia-stream-test-", ".db")
        testDbFile.deleteOnExit()

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

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            setupData?.invoke()

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

    private fun insertTestUser(userId: String = "user-1") {
        transaction {
            UsersTable.insert {
                it[id] = userId
                it[email] = "$userId@example.com"
                it[passwordHash] = "hash"
                it[displayName] = "Test User"
                it[role] = "CREATOR"
                it[authProvider] = "EMAIL"
                it[createdAt] = Instant.now().toString()
                it[updatedAt] = Instant.now().toString()
            }
        }
    }

    private fun insertTestCategory(categoryId: String = "cat-1") {
        transaction {
            CategoriesTable.insert {
                it[id] = categoryId
                it[name] = "Test Category $categoryId"
                it[slug] = "test-category-$categoryId"
            }
        }
    }

    private fun insertTestVideo(
        videoId: String,
        videoUrl: String? = null,
        thumbnailUrl: String? = null,
        status: String = "PUBLISHED",
        creatorId: String = "user-1"
    ) {
        transaction {
            VideosTable.insert {
                it[id] = videoId
                it[VideosTable.creatorId] = creatorId
                it[title] = "Test Video"
                it[categoryId] = "cat-1"
                it[rawFilePath] = "/tmp/raw.mp4"
                it[VideosTable.videoUrl] = videoUrl
                it[VideosTable.thumbnailUrl] = thumbnailUrl
                it[VideosTable.status] = status
                it[createdAt] = Instant.now().toString()
                it[updatedAt] = Instant.now().toString()
            }
        }
    }

    @Test
    fun `GET stream returns 404 for non-existent video`() = testApp(
        setupData = null
    ) { client ->
        val response = client.get("/api/v1/videos/nonexistent/stream")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET stream returns 404 for unprocessed video`() = testApp(setupData = {
        insertTestUser()
        insertTestCategory()
        insertTestVideo("vid-1", videoUrl = null, status = "PUBLISHED")
    }) { client ->
        val response = client.get("/api/v1/videos/vid-1/stream")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET stream returns video file content for published video`() {
        val tempVideoFile = File.createTempFile("test-video-", ".mp4")
        tempVideoFile.deleteOnExit()
        val testContent = "fake video content for testing"
        tempVideoFile.writeText(testContent)

        testApp(setupData = {
            insertTestUser()
            insertTestCategory()
            insertTestVideo("vid-2", videoUrl = tempVideoFile.absolutePath, status = "PUBLISHED")
        }) { client ->
            val response = client.get("/api/v1/videos/vid-2/stream")
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertEquals(testContent, body)
        }

        tempVideoFile.delete()
    }

    @Test
    fun `GET stream with Range header returns partial content`() {
        val tempVideoFile = File.createTempFile("test-video-range-", ".mp4")
        tempVideoFile.deleteOnExit()
        val testContent = "0123456789abcdefghij"
        tempVideoFile.writeText(testContent)

        testApp(setupData = {
            insertTestUser()
            insertTestCategory()
            insertTestVideo("vid-3", videoUrl = tempVideoFile.absolutePath, status = "PUBLISHED")
        }) { client ->
            val response = client.get("/api/v1/videos/vid-3/stream") {
                header(HttpHeaders.Range, "bytes=0-9")
            }
            assertEquals(HttpStatusCode.PartialContent, response.status)
            val body = response.bodyAsText()
            assertEquals("0123456789", body)

            val contentRange = response.headers[HttpHeaders.ContentRange]
            assertTrue(contentRange != null && contentRange.startsWith("bytes 0-9/"))
        }

        tempVideoFile.delete()
    }

    @Test
    fun `GET stream returns 404 for non-published video without auth`() = testApp(setupData = {
        val tempVideoFile = File.createTempFile("test-video-draft-", ".mp4")
        tempVideoFile.deleteOnExit()
        tempVideoFile.writeText("draft video")

        insertTestUser()
        insertTestCategory()
        insertTestVideo("vid-4", videoUrl = tempVideoFile.absolutePath, status = "DRAFT")
    }) { client ->
        val response = client.get("/api/v1/videos/vid-4/stream")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET thumbnail returns 404 for non-existent video`() = testApp(
        setupData = null
    ) { client ->
        val response = client.get("/api/v1/videos/nonexistent/thumbnail")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET thumbnail returns 404 when no thumbnail available`() = testApp(setupData = {
        insertTestUser()
        insertTestCategory()
        insertTestVideo("vid-5", thumbnailUrl = null, status = "PUBLISHED")
    }) { client ->
        val response = client.get("/api/v1/videos/vid-5/thumbnail")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `GET thumbnail returns file when available`() {
        val tempThumbFile = File.createTempFile("test-thumb-", ".jpg")
        tempThumbFile.deleteOnExit()
        val testContent = "fake thumbnail data"
        tempThumbFile.writeBytes(testContent.toByteArray())

        testApp(setupData = {
            insertTestUser()
            insertTestCategory()
            insertTestVideo("vid-6", thumbnailUrl = tempThumbFile.absolutePath, status = "PUBLISHED")
        }) { client ->
            val response = client.get("/api/v1/videos/vid-6/thumbnail")
            assertEquals(HttpStatusCode.OK, response.status)
        }

        tempThumbFile.delete()
    }
}
