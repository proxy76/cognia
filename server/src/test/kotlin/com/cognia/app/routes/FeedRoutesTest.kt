package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.feed.FeedResponse
import com.cognia.app.dto.feed.TrackViewRequest
import com.cognia.app.dto.feed.TrackingResponse
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
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FeedRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-feed-test-", ".db")
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
            single { PreferenceRepository() }
            single { PreferenceService(get(), get()) }
            single { FeedRepository() }
            single { FeedService(get(), get()) }
            single { TrackingService() }
            single { SearchService() }
            single { VideoRepository() }
            single { VideoService(get()) }
        }

        install(Koin) {
            modules(testModule)
        }

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            // Seed test data
            transaction {
                CategoriesTable.insert {
                    it[id] = TEST_CATEGORY_ID
                    it[name] = "Science"
                    it[slug] = "science"
                }
                CategoriesTable.insert {
                    it[id] = TEST_CATEGORY_ID_2
                    it[name] = "History"
                    it[slug] = "history"
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
        email: String = "feeduser@example.com",
        password: String = "password123",
        displayName: String = "Feed User"
    ): Pair<String, String> {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(email, password, displayName))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        val auth = response.body<AuthResponse>()
        return Pair(auth.token, auth.userId)
    }

    private fun seedPublishedVideo(creatorId: String, categoryId: String, title: String): String {
        val videoId = java.util.UUID.randomUUID().toString()
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.UTC).toString()

        transaction {
            VideosTable.insert {
                it[id] = videoId
                it[VideosTable.creatorId] = creatorId
                it[VideosTable.title] = title
                it[VideosTable.categoryId] = categoryId
                it[status] = "PUBLISHED"
                it[createdAt] = now
                it[updatedAt] = now
                it[publishedAt] = now
            }
        }
        return videoId
    }

    @Test
    fun `GET foryou feed returns published videos`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        // Seed published videos
        seedPublishedVideo(userId, TEST_CATEGORY_ID, "Quantum Physics 101")
        seedPublishedVideo(userId, TEST_CATEGORY_ID_2, "Ancient Rome")

        val response = client.get("/api/v1/feed/foryou?page=1&limit=20") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val feed = response.body<FeedResponse>()
        assertEquals(2, feed.items.size)
        assertEquals(1, feed.page)
        assertFalse(feed.hasMore)
    }

    @Test
    fun `GET deepdive feed returns only user category videos`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        // Set user preferences to Science
        transaction {
            UserCategoriesTable.insert {
                it[UserCategoriesTable.userId] = userId
                it[categoryId] = TEST_CATEGORY_ID
            }
        }

        // Seed videos in both categories
        seedPublishedVideo(userId, TEST_CATEGORY_ID, "Quantum Physics 101")
        seedPublishedVideo(userId, TEST_CATEGORY_ID_2, "Ancient Rome")

        val response = client.get("/api/v1/feed/deepdive?page=1&limit=20") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val feed = response.body<FeedResponse>()
        assertEquals(1, feed.items.size)
        assertEquals("Quantum Physics 101", feed.items[0].title)
        assertEquals("Science", feed.items[0].categoryName)
    }

    @Test
    fun `GET feed requires authentication`() = testApp { client ->
        val response = client.get("/api/v1/feed/foryou")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST tracking view returns success`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)
        val videoId = seedPublishedVideo(userId, TEST_CATEGORY_ID, "Test Video")

        val response = client.post("/api/v1/tracking/view") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(TrackViewRequest(contentId = videoId, contentType = "VIDEO"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val result = response.body<TrackingResponse>()
        assertTrue(result.success)

        // Verify the view was tracked
        val viewCount = transaction {
            ContentViewsTable.select(ContentViewsTable.id)
                .where { ContentViewsTable.contentId eq videoId }
                .count()
        }
        assertEquals(1L, viewCount)
    }

    companion object {
        private const val TEST_CATEGORY_ID = "feed-cat-001"
        private const val TEST_CATEGORY_ID_2 = "feed-cat-002"
    }
}
