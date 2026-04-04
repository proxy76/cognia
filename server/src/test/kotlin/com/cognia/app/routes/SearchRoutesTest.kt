package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.search.RecentSearchesResponse
import com.cognia.app.dto.search.SaveSearchRequest
import com.cognia.app.dto.search.SearchResultsResponse
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
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SearchRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-search-test-", ".db")
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
            single { SearchService() }
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
        email: String = "searcher@example.com",
        password: String = "password123",
        displayName: String = "Search User"
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
    fun `GET search returns matching videos`() = testApp { client ->
        val (token, userId) = registerAndGetToken(client)

        // Seed a published video
        val now = Clock.System.now()
            .toLocalDateTime(TimeZone.UTC).toString()
        transaction {
            VideosTable.insert {
                it[id] = "search-video-1"
                it[creatorId] = userId
                it[title] = "Quantum Physics Explained"
                it[description] = "A deep dive into quantum mechanics"
                it[categoryId] = TEST_CATEGORY_ID
                it[status] = "PUBLISHED"
                it[createdAt] = now
                it[updatedAt] = now
                it[publishedAt] = now
            }
        }

        val response = client.get("/api/v1/search?q=quantum&type=videos") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val results = response.body<SearchResultsResponse>()
        assertEquals("quantum", results.query)
        assertEquals(1, results.results.videos.size)
        assertEquals("Quantum Physics Explained", results.results.videos[0].title)
    }

    @Test
    fun `GET search returns matching categories`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/search?q=science&type=categories") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val results = response.body<SearchResultsResponse>()
        assertEquals(1, results.results.categories.size)
        assertEquals("Science", results.results.categories[0].name)
    }

    @Test
    fun `GET search without query returns 400`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        val response = client.get("/api/v1/search") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `POST and GET search history`() = testApp { client ->
        val (token, _) = registerAndGetToken(client)

        // Save a search
        val saveResponse = client.post("/api/v1/search/history") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(SaveSearchRequest(query = "quantum physics"))
        }
        assertEquals(HttpStatusCode.Created, saveResponse.status)

        // Get history
        val historyResponse = client.get("/api/v1/search/history") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
        assertEquals(HttpStatusCode.OK, historyResponse.status)
        val history = historyResponse.body<RecentSearchesResponse>()
        assertTrue(history.recentSearches.contains("quantum physics"))
    }

    @Test
    fun `GET search requires authentication`() = testApp { client ->
        val response = client.get("/api/v1/search?q=test")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    companion object {
        private const val TEST_CATEGORY_ID = "search-cat-001"
    }
}
