package com.cognia.app.dto.quiz

import kotlinx.serialization.Serializable

@Serializable
data class QuizCreateRequest(
    val title: String,
    val quizType: String,
    val categoryId: String,
    val videoId: String? = null,
    val difficulty: String? = null,
    val questions: List<QuizQuestionCreate>
)

@Serializable
data class QuizQuestionCreate(
    val questionText: String,
    val options: List<QuizOptionCreate>,
    val correctOptionIndex: Int
)

@Serializable
data class QuizOptionCreate(val text: String)

@Serializable
data class QuizResponse(
    val id: String,
    val title: String,
    val quizType: String,
    val difficulty: String? = null,
    val questions: List<QuizQuestionResponse>
)

@Serializable
data class QuizQuestionResponse(
    val id: String,
    val questionText: String,
    val options: List<QuizOptionResponse>
)

@Serializable
data class QuizOptionResponse(
    val id: String,
    val text: String
)

@Serializable
data class QuizAttemptRequest(
    val answers: List<QuizAnswerSubmit>
)

@Serializable
data class QuizAnswerSubmit(
    val questionId: String,
    val selectedOptionIndex: Int
)

@Serializable
data class QuizAttemptResponse(
    val score: Int,
    val totalQuestions: Int,
    val pointsAwarded: Long,
    val results: List<QuizAnswerResult>
)

@Serializable
data class QuizAnswerResult(
    val questionId: String,
    val correct: Boolean,
    val correctOptionIndex: Int
)

@Serializable
data class QuizStatusResponse(
    val id: String,
    val status: String
)
