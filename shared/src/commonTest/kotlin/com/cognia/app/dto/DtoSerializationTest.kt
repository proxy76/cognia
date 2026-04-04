package com.cognia.app.dto

import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.chat.ChatMessageResponse
import com.cognia.app.dto.content.VideoResponse
import com.cognia.app.dto.notification.NotificationResponse
import com.cognia.app.dto.quiz.QuizAnswerResult
import com.cognia.app.dto.quiz.QuizAttemptResponse
import com.cognia.app.dto.quiz.QuizCreateRequest
import com.cognia.app.dto.quiz.QuizOptionCreate
import com.cognia.app.dto.quiz.QuizQuestionCreate
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
        val original = QuizCreateRequest(
            title = "Kotlin Basics Quiz",
            quizType = "MULTIPLE_CHOICE",
            categoryId = "cat-1",
            videoId = "video-1",
            difficulty = "BEGINNER",
            questions = listOf(
                QuizQuestionCreate(
                    questionText = "What is val?",
                    options = listOf(
                        QuizOptionCreate(text = "Immutable variable"),
                        QuizOptionCreate(text = "Mutable variable"),
                        QuizOptionCreate(text = "Function"),
                        QuizOptionCreate(text = "Class")
                    ),
                    correctOptionIndex = 0
                )
            )
        )
        val encoded = json.encodeToString(QuizCreateRequest.serializer(), original)
        val decoded = json.decodeFromString(QuizCreateRequest.serializer(), encoded)
        assertEquals(original, decoded)
    }

    @Test
    fun quizAttemptResponseRoundTrip() {
        val original = QuizAttemptResponse(
            score = 3,
            totalQuestions = 5,
            pointsAwarded = 150,
            results = listOf(
                QuizAnswerResult(questionId = "q-1", correct = true, correctOptionIndex = 0),
                QuizAnswerResult(questionId = "q-2", correct = false, correctOptionIndex = 2),
                QuizAnswerResult(questionId = "q-3", correct = true, correctOptionIndex = 1),
                QuizAnswerResult(questionId = "q-4", correct = true, correctOptionIndex = 3),
                QuizAnswerResult(questionId = "q-5", correct = false, correctOptionIndex = 0)
            )
        )
        val encoded = json.encodeToString(QuizAttemptResponse.serializer(), original)
        val decoded = json.decodeFromString(QuizAttemptResponse.serializer(), encoded)
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
