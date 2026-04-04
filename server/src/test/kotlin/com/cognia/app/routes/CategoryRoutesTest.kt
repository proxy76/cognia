package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.database.UsersTable
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.category.CreateCategoryRequest
import com.cognia.app.dto.category.UpdateCategoryRequest
import com.cognia.app.plugins.*
import com.cognia.app.repository.CategoryRepository
import com.cognia.app.repository.PreferenceRepository
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CategoryRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(
        seedCategories: Boolean = true,
        block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit
    ) = testApplication {
        val testDbFile = File.createTempFile("cognia-cat-test-", ".db")
        testDbFile.deleteOnExit()

        application {
            DatabaseFactory.init(testDbFile.absolutePath)

            val categoryRepository = CategoryRepository()
            val categoryService = CategoryService(categoryRepository)

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
                single { categoryRepository }
                single { categoryService }
                single { PreferenceRepository() }
                single { PreferenceService(get(), get()) }
            }

            install(Koin) {
                modules(testModule)
            }

            configureSerialization()
            configureStatusPages()
            configureAuth()
            configureWebSockets()
            configureRouting()

            if (seedCategories) {
                categoryService.seedCategories()
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

    private suspend fun registerAndGetAdminToken(client: io.ktor.client.HttpClient): String {
        val authService = AuthService(
            UserRepository(),
            RefreshTokenRepository(),
            testConfig
        )
        // Register via API
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(com.cognia.app.dto.auth.RegisterRequest("admin@test.com", "password123", "Admin User"))
        }
        val authResponse = response.body<com.cognia.app.dto.auth.AuthResponse>()

        // Make the user an admin by updating the DB directly
        transaction {
            UsersTable.update({ UsersTable.id eq authResponse.userId }) {
                it[UsersTable.role] = "ADMIN"
            }
        }

        // Generate a new token with the ADMIN role
        val adminAuth = authService.generateAuthResponse(authResponse.userId, "admin@test.com", "ADMIN")
        return adminAuth.token
    }

    @Test
    fun `GET categories returns seeded categories`() = testApp { client ->
        val response = client.get("/api/v1/categories")
        assertEquals(HttpStatusCode.OK, response.status)

        val categories = response.body<List<CategoryResponse>>()
        assertEquals(20, categories.size)
        assertTrue(categories.any { it.name == "Science" })
        assertTrue(categories.any { it.name == "Mathematics" })
        assertTrue(categories.any { it.name == "Computer Science" })
    }

    @Test
    fun `GET categories by id returns specific category`() = testApp { client ->
        // First get all categories to find an ID
        val allResponse = client.get("/api/v1/categories")
        val categories = allResponse.body<List<CategoryResponse>>()
        val first = categories.first()

        val response = client.get("/api/v1/categories/${first.id}")
        assertEquals(HttpStatusCode.OK, response.status)

        val category = response.body<CategoryResponse>()
        assertEquals(first.id, category.id)
        assertEquals(first.name, category.name)
        assertEquals(first.slug, category.slug)
    }

    @Test
    fun `GET categories by invalid id returns 404`() = testApp { client ->
        val response = client.get("/api/v1/categories/nonexistent-id")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `POST create category as admin returns 201`() = testApp { client ->
        val token = registerAndGetAdminToken(client)

        val response = client.post("/api/v1/categories") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(CreateCategoryRequest("Astronomy", "astronomy"))
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val category = response.body<CategoryResponse>()
        assertEquals("Astronomy", category.name)
        assertEquals("astronomy", category.slug)
        assertNotNull(category.id)
    }

    @Test
    fun `POST create category with duplicate name returns 409`() = testApp { client ->
        val token = registerAndGetAdminToken(client)

        val response = client.post("/api/v1/categories") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(CreateCategoryRequest("Science", "science-duplicate"))
        }

        assertEquals(HttpStatusCode.Conflict, response.status)
    }

    @Test
    fun `DELETE category returns 204`() = testApp { client ->
        val token = registerAndGetAdminToken(client)

        // Get a category ID
        val allResponse = client.get("/api/v1/categories")
        val categories = allResponse.body<List<CategoryResponse>>()
        val toDelete = categories.last()

        val response = client.delete("/api/v1/categories/${toDelete.id}") {
            header("Authorization", "Bearer $token")
        }

        assertEquals(HttpStatusCode.NoContent, response.status)

        // Verify it's gone
        val getResponse = client.get("/api/v1/categories/${toDelete.id}")
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }

    @Test
    fun `POST create category without auth returns 401`() = testApp { client ->
        val response = client.post("/api/v1/categories") {
            contentType(ContentType.Application.Json)
            setBody(CreateCategoryRequest("Astronomy", "astronomy"))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `POST create category as non-admin returns 403`() = testApp { client ->
        // Register a regular user
        val registerResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(com.cognia.app.dto.auth.RegisterRequest("learner@test.com", "password123", "Learner User"))
        }
        val authResponse = registerResponse.body<com.cognia.app.dto.auth.AuthResponse>()

        val response = client.post("/api/v1/categories") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${authResponse.token}")
            setBody(CreateCategoryRequest("Astronomy", "astronomy"))
        }

        assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
