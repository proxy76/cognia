package com.cognia.app.dto.ai

import com.cognia.app.dto.category.CategoryResponse
import kotlinx.serialization.Serializable

// ── Categorization ──────────────────────────────────────────────────

@Serializable
data class CategorizationRequest(
    val title: String,
    val description: String? = null
)

@Serializable
data class CategoryMatch(
    val category: CategoryResponse,
    val confidence: Double
)

@Serializable
data class CategorizationResponse(
    val matches: List<CategoryMatch>
)

// ── Content Moderation ──────────────────────────────────────────────

@Serializable
data class AiModerationRequest(
    val contentId: String,
    val contentType: String,
    val title: String,
    val description: String? = null
)

@Serializable
data class AiModerationResponse(
    val assessment: String,
    val confidence: Double,
    val reason: String
)

// ── Quiz Generation ─────────────────────────────────────────────────

@Serializable
data class QuizGenerateRequest(
    val videoId: String? = null,
    val title: String,
    val description: String? = null,
    val categoryId: String,
    val questionCount: Int = 5,
    val difficulty: String? = null
)

@Serializable
data class GeneratedQuestion(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

@Serializable
data class QuizGenerateResponse(
    val title: String,
    val questions: List<GeneratedQuestion>
)
