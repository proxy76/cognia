package com.cognia.app.dto.onboarding

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
data class CategoryResponse(val id: String, val name: String, val slug: String)

@Serializable
data class SavePreferencesRequest(
    val categoryIds: List<String>,
    val selfDescription: String? = null
)

@Serializable
data class CategoriesResponse(val categories: List<CategoryResponse>)
