package com.cognia.app.dto.quiz

import kotlinx.serialization.Serializable

// ── Create DTOs ────────────────────────────────────────────────────

@Serializable
data class CreateQuizRequest(
    val title: String,
    val quizType: String,
    val categoryId: String,
    val difficulty: String? = null,
    val videoId: String? = null,
    val questions: List<CreateQuestionRequest>
)

@Serializable
data class CreateQuestionRequest(
    val questionText: String,
    val correctOptionIndex: Int,
    val options: List<CreateOptionRequest>
)

@Serializable
data class CreateOptionRequest(val text: String)

// ── Response DTOs ──────────────────────────────────────────────────

@Serializable
data class QuizDetailResponse(
    val id: String,
    val creatorId: String,
    val title: String,
    val quizType: String,
    val categoryId: String,
    val difficulty: String? = null,
    val videoId: String? = null,
    val status: String,
    val questions: List<QuestionResponse>
)

@Serializable
data class QuestionResponse(
    val id: String,
    val questionText: String,
    val options: List<OptionResponse>,
    val correctOptionIndex: Int? = null // null when hidden from non-owner
)

@Serializable
data class OptionResponse(val id: String, val text: String)

@Serializable
data class QuizSummaryResponse(
    val id: String,
    val title: String,
    val quizType: String,
    val status: String,
    val questionCount: Int
)

// ── Attempt DTOs ───────────────────────────────────────────────────

@Serializable
data class QuizAttemptRequest(val answers: List<Int>)

@Serializable
data class AttemptResultResponse(
    val score: Int,
    val totalQuestions: Int,
    val pointsAwarded: Int,
    val results: List<QuestionResult>
)

@Serializable
data class QuestionResult(
    val questionId: String,
    val selectedIndex: Int,
    val correctIndex: Int,
    val isCorrect: Boolean
)
