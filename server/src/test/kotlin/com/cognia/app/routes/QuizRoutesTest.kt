package com.cognia.app.routes

import com.cognia.app.config.AppConfig
import com.cognia.app.database.*
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.quiz.*
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
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizRoutesTest {

    private val testConfig = AppConfig.fromEnvironment()

    private fun testApp(block: suspend ApplicationTestBuilder.(io.ktor.client.HttpClient) -> Unit) = testApplication {
        val testDbFile = File.createTempFile("cognia-quiz-test-", ".db")
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
            single { QuizRepository() }
            single { QuizAttemptRepository() }
            single { QuizService(get()) }
            single { ScoringService(get(), get()) }
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

    private fun createQuizRequest(
        title: String = "Kotlin Quiz",
        difficulty: String? = "EASY"
    ) = CreateQuizRequest(
        title = title,
        quizType = "MULTIPLE_CHOICE",
        categoryId = TEST_CATEGORY_ID,
        difficulty = difficulty,
        videoId = null,
        questions = listOf(
            CreateQuestionRequest(
                questionText = "What is val?",
                correctOptionIndex = 0,
                options = listOf(
                    CreateOptionRequest("Immutable variable"),
                    CreateOptionRequest("Mutable variable"),
                    CreateOptionRequest("Function")
                )
            ),
            CreateQuestionRequest(
                questionText = "What is var?",
                correctOptionIndex = 1,
                options = listOf(
                    CreateOptionRequest("Immutable variable"),
                    CreateOptionRequest("Mutable variable"),
                    CreateOptionRequest("Class")
                )
            )
        )
    )

    // ── Create Quiz Tests ──────────────────────────────────────────

    @Test
    fun `POST create quiz returns 201`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val response = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest())
        }

        assertEquals(HttpStatusCode.Created, response.status)
        val quiz = response.body<QuizDetailResponse>()
        assertEquals("Kotlin Quiz", quiz.title)
        assertEquals("DRAFT", quiz.status)
        assertEquals("EASY", quiz.difficulty)
        assertEquals(2, quiz.questions.size)
        // Owner should see correctOptionIndex
        assertNotNull(quiz.questions[0].correctOptionIndex)
        assertEquals(0, quiz.questions[0].correctOptionIndex)
    }

    // ── Get Quiz Tests ─────────────────────────────────────────────

    @Test
    fun `GET quiz hides answers for non-owner`() = testApp { client ->
        // Create as creator
        val (_, creatorId) = registerAndGetToken(client)
        val creatorToken = setRoleAndGetToken(client, creatorId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $creatorToken")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest())
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Register another user
        val (_, otherId) = registerAndGetToken(client, email = "other@example.com", displayName = "Other")
        val otherToken = setRoleAndGetToken(client, otherId, "REGULAR_CREATOR", email = "other@example.com")

        // Get as non-owner
        val response = client.get("/api/v1/quizzes/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $otherToken")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val quiz = response.body<QuizDetailResponse>()
        // correctOptionIndex should be null for non-owner
        assertNull(quiz.questions[0].correctOptionIndex)
    }

    @Test
    fun `GET quiz shows answers for owner`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest())
        }
        val created = createResponse.body<QuizDetailResponse>()

        val response = client.get("/api/v1/quizzes/${created.id}") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val quiz = response.body<QuizDetailResponse>()
        assertNotNull(quiz.questions[0].correctOptionIndex)
    }

    // ── Submit for Review Tests ────────────────────────────────────

    @Test
    fun `POST submit transitions status to PENDING_REVIEW`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest())
        }
        val created = createResponse.body<QuizDetailResponse>()

        val response = client.post("/api/v1/quizzes/${created.id}/submit") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val quiz = response.body<QuizDetailResponse>()
        assertEquals("PENDING_REVIEW", quiz.status)
    }

    // ── Attempt Tests ──────────────────────────────────────────────

    @Test
    fun `POST attempt scores correctly`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        // Create quiz
        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest())
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Submit attempt: answer 0 for Q1 (correct), answer 0 for Q2 (wrong, correct is 1)
        val attemptResponse = client.post("/api/v1/quizzes/${created.id}/attempt") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(QuizAttemptRequest(answers = listOf(0, 0)))
        }

        assertEquals(HttpStatusCode.OK, attemptResponse.status)
        val result = attemptResponse.body<AttemptResultResponse>()
        assertEquals(1, result.score)
        assertEquals(2, result.totalQuestions)
        assertTrue(result.results[0].isCorrect)
        assertEquals(false, result.results[1].isCorrect)
    }

    @Test
    fun `POST attempt with EASY difficulty awards 10 points per correct`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest(difficulty = "EASY"))
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Both answers correct
        val attemptResponse = client.post("/api/v1/quizzes/${created.id}/attempt") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(QuizAttemptRequest(answers = listOf(0, 1)))
        }

        val result = attemptResponse.body<AttemptResultResponse>()
        assertEquals(2, result.score)
        assertEquals(20, result.pointsAwarded) // 2 * 10 (EASY)
    }

    @Test
    fun `POST attempt with HARD difficulty awards 30 points per correct`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest(difficulty = "HARD"))
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Both answers correct
        val attemptResponse = client.post("/api/v1/quizzes/${created.id}/attempt") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(QuizAttemptRequest(answers = listOf(0, 1)))
        }

        val result = attemptResponse.body<AttemptResultResponse>()
        assertEquals(2, result.score)
        assertEquals(60, result.pointsAwarded) // 2 * 30 (HARD)
    }

    @Test
    fun `POST attempt updates user profile points and level`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest(difficulty = "MEDIUM"))
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Both answers correct => 2 * 20 = 40 points
        client.post("/api/v1/quizzes/${created.id}/attempt") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(QuizAttemptRequest(answers = listOf(0, 1)))
        }

        // Check user profile was updated
        val profile = transaction {
            UserProfilesTable.selectAll()
                .where { UserProfilesTable.userId eq userId }
                .singleOrNull()
        }
        assertNotNull(profile)
        assertEquals(40, profile[UserProfilesTable.totalPoints])
        assertEquals(1, profile[UserProfilesTable.level]) // 40 / 100 + 1 = 1
    }

    @Test
    fun `POST attempt with no difficulty awards base 10 points`() = testApp { client ->
        val (_, userId) = registerAndGetToken(client)
        val token = setRoleAndGetToken(client, userId, "REGULAR_CREATOR")

        val createResponse = client.post("/api/v1/quizzes") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(createQuizRequest(difficulty = null))
        }
        val created = createResponse.body<QuizDetailResponse>()

        // Both answers correct
        val attemptResponse = client.post("/api/v1/quizzes/${created.id}/attempt") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(QuizAttemptRequest(answers = listOf(0, 1)))
        }

        val result = attemptResponse.body<AttemptResultResponse>()
        assertEquals(20, result.pointsAwarded) // 2 * 10 (base)
    }

    companion object {
        private const val TEST_CATEGORY_ID = "test-cat-001"
    }
}
