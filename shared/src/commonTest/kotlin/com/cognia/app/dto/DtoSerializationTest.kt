package com.cognia.app.dto

import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.chat.ChatMessageResponse
import com.cognia.app.dto.content.VideoResponse
import com.cognia.app.dto.notification.NotificationResponse
import com.cognia.app.dto.quiz.QuestionResult
import com.cognia.app.dto.quiz.AttemptResultResponse
import com.cognia.app.dto.quiz.CreateQuizRequest
import com.cognia.app.dto.quiz.CreateOptionRequest
import com.cognia.app.dto.quiz.CreateQuestionRequest
import com.cognia.app.dto.user.CategorySummary
import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class DtoSerializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun registerRequestRoundTrip() {
        val original = RegisterRequest(
            email = "test@example.com",
            password = "secret123",
            displayName = "Test User"
        )
        val encoded = json.encodeToString(RegisterRequest.serializer(), original)
        val decoded = json.decodeFromString(RegisterRequest.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun loginRequestRoundTrip() {
        val original = LoginRequest(
            email = "test@example.com",
            password = "secret123"
        )
        val encoded = json.encodeToString(LoginRequest.serializer(), original)
        val decoded = json.decodeFromString(LoginRequest.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun authResponseRoundTrip() {
        val original = AuthResponse(
            userId = "user-123",
            token = "jwt-token-abc",
            refreshToken = "refresh-token-xyz"
        )
        val encoded = json.encodeToString(AuthResponse.serializer(), original)
        val decoded = json.decodeFromString(AuthResponse.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun videoResponseRoundTrip() {
        val original = VideoResponse(
            id = "video-1",
            creator = UserSummary(id = "user-1", displayName = "Creator", avatarUrl = null),
            title = "Learn Kotlin",
            description = "A great video about Kotlin",
            category = CategorySummary(id = "cat-1", name = "Programming"),
            videoUrl = "https://example.com/video.mp4",
            thumbnailUrl = "https://example.com/thumb.jpg",
            difficulty = "INTERMEDIATE",
            publishedAt = "2026-01-15T10:30:00Z",
            viewCount = 1500,
            quizId = "quiz-1"
        )
        val encoded = json.encodeToString(VideoResponse.serializer(), original)
        val decoded = json.decodeFromString(VideoResponse.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun quizCreateRequestRoundTrip() {
        val original = CreateQuizRequest(
            title = "Kotlin Basics Quiz",
            quizType = "MULTIPLE_CHOICE",
            categoryId = "cat-1",
            videoId = "video-1",
            difficulty = "BEGINNER",
            questions = listOf(
                CreateQuestionRequest(
                    questionText = "What is val?",
                    options = listOf(
                        CreateOptionRequest(text = "Immutable variable"),
                        CreateOptionRequest(text = "Mutable variable"),
                        CreateOptionRequest(text = "Function"),
                        CreateOptionRequest(text = "Class")
                    ),
                    correctOptionIndex = 0
                )
            )
        )
        val encoded = json.encodeToString(CreateQuizRequest.serializer(), original)
        val decoded = json.decodeFromString(CreateQuizRequest.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun quizAttemptResponseRoundTrip() {
        val original = AttemptResultResponse(
            score = 3,
            totalQuestions = 5,
            pointsAwarded = 150,
            results = listOf(
                QuestionResult(questionId = "q-1", selectedIndex = 0, correctIndex = 0, isCorrect = true),
                QuestionResult(questionId = "q-2", selectedIndex = 1, correctIndex = 2, isCorrect = false),
                QuestionResult(questionId = "q-3", selectedIndex = 1, correctIndex = 1, isCorrect = true),
                QuestionResult(questionId = "q-4", selectedIndex = 3, correctIndex = 3, isCorrect = true),
                QuestionResult(questionId = "q-5", selectedIndex = 1, correctIndex = 0, isCorrect = false)
            )
        )
        val encoded = json.encodeToString(AttemptResultResponse.serializer(), original)
        val decoded = json.decodeFromString(AttemptResultResponse.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun chatMessageResponseRoundTrip() {
        val original = ChatMessageResponse(
            id = "msg-1",
            senderId = "user-1",
            messageType = "TEXT",
            textContent = "Hello there!",
            sharedContentId = null,
            sharedContentType = null,
            createdAt = "2026-03-20T14:00:00Z"
        )
        val encoded = json.encodeToString(ChatMessageResponse.serializer(), original)
        val decoded = json.decodeFromString(ChatMessageResponse.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun notificationResponseRoundTrip() {
        val original = NotificationResponse(
            id = "notif-1",
            type = "NEW_FOLLOWER",
            title = "New follower",
            body = "User X started following you",
            referenceId = "user-2",
            referenceType = "USER",
            read = false,
            createdAt = "2026-03-21T09:00:00Z"
        )
        val encoded = json.encodeToString(NotificationResponse.serializer(), original)
        val decoded = json.decodeFromString(NotificationResponse.serializer(), encoded)
        assertEquals(original, decoded)
    }
}
