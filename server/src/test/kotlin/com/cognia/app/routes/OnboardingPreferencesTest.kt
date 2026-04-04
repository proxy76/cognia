package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.onboarding.PreferenceResponse
import com.cognia.app.dto.onboarding.SavePreferencesRequest
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
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OnboardingPreferencesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-pref-test-", ".db")
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
            configureRouting()

            categoryService.seedCategories()
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

    private suspend fun registerAndGetToken(client: io.ktor.client.HttpClient): String {
        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(com.cognia.app.dto.auth.RegisterRequest("user@test.com", "password123", "Test User"))
        }
        val authResponse = response.body<com.cognia.app.dto.auth.AuthResponse>()
        return authResponse.token
    }

    @Test
    fun `PUT preferences saves categories`() = testApp { client ->
        val token = registerAndGetToken(client)

        // Get categories to use their IDs
        val categoriesResponse = client.get("/api/v1/categories")
        val categories = categoriesResponse.body<List<CategoryResponse>>()
        val selectedIds = categories.take(3).map { it.id }

        val response = client.put("/api/v1/onboarding/preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(SavePreferencesRequest(categoryIds = selectedIds, selfDescription = "I love science"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val prefs = response.body<PreferenceResponse>()
        assertEquals(3, prefs.categories.size)
        assertEquals("I love science", prefs.selfDescription)
    }

    @Test
    fun `GET preferences returns saved preferences`() = testApp { client ->
        val token = registerAndGetToken(client)

        // Get categories
        val categoriesResponse = client.get("/api/v1/categories")
        val categories = categoriesResponse.body<List<CategoryResponse>>()
        val selectedIds = categories.take(2).map { it.id }

        // Save preferences
        client.put("/api/v1/onboarding/preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(SavePreferencesRequest(categoryIds = selectedIds, selfDescription = "Math enthusiast"))
        }

        // Get preferences
        val response = client.get("/api/v1/onboarding/preferences") {
            header("Authorization", "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val prefs = response.body<PreferenceResponse>()
        assertEquals(2, prefs.categories.size)
        assertEquals("Math enthusiast", prefs.selfDescription)
    }

    @Test
    fun `PUT preferences with invalid category IDs returns 400`() = testApp { client ->
        val token = registerAndGetToken(client)

        val response = client.put("/api/v1/onboarding/preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(SavePreferencesRequest(categoryIds = listOf("nonexistent-id"), selfDescription = null))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `PUT preferences unauthenticated returns 401`() = testApp { client ->
        val response = client.put("/api/v1/onboarding/preferences") {
            contentType(ContentType.Application.Json)
            setBody(SavePreferencesRequest(categoryIds = emptyList(), selfDescription = null))
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `PUT preferences with empty category list succeeds`() = testApp { client ->
        val token = registerAndGetToken(client)

        val response = client.put("/api/v1/onboarding/preferences") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(SavePreferencesRequest(categoryIds = emptyList(), selfDescription = "Just browsing"))
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val prefs = response.body<PreferenceResponse>()
        assertEquals(0, prefs.categories.size)
        assertEquals("Just browsing", prefs.selfDescription)
    }
}
