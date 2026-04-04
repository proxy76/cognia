package com.cognia.app.dto.onboarding

import com.cognia.app.dto.category.CategoryResponse
import kotlinx.serialization.Serializable

@Serializable
data class OnboardingAnswer(val questionId: String, val answer: String)

@Serializable
data class RecommendRequest(
    val selfDescription: String,
    val answers: List<OnboardingAnswer> = emptyList()
)

@Serializable
data class RecommendResponse(
    val recommendedCategories: List<CategoryResponse>
)

@Serializable
data class SavePreferencesRequest(
    val categoryIds: List<String>,
    val selfDescription: String? = null
)

@Serializable
data class PreferenceResponse(
    val categories: List<CategoryResponse>,
    val selfDescription: String?
)

@Serializable
data class CategoriesResponse(val categories: List<CategoryResponse>)
