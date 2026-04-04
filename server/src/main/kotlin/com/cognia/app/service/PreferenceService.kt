package com.cognia.app.service

import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.onboarding.PreferenceResponse
import com.cognia.app.dto.onboarding.SavePreferencesRequest
import com.cognia.app.repository.CategoryRepository
import com.cognia.app.repository.PreferenceRepository

class PreferenceService(
    private val preferenceRepository: PreferenceRepository,
    private val categoryRepository: CategoryRepository
) {

    fun getPreferences(userId: String): PreferenceResponse {
        val categories = preferenceRepository.getCategories(userId)
        val selfDescription = preferenceRepository.getSelfDescription(userId)
        return PreferenceResponse(
            categories = categories.map { CategoryResponse(id = it.id, name = it.name, slug = it.slug) },
            selfDescription = selfDescription
        )
    }

    fun savePreferences(userId: String, request: SavePreferencesRequest): PreferenceResponse {
        // Validate that all categoryIds exist
        if (request.categoryIds.isNotEmpty()) {
            val existingCategories = categoryRepository.findByIds(request.categoryIds)
            val existingIds = existingCategories.map { it.id }.toSet()
            val invalidIds = request.categoryIds.filter { it !in existingIds }
            if (invalidIds.isNotEmpty()) {
                throw InvalidCategoryIdsException(invalidIds)
            }
        }

        preferenceRepository.savePreferences(userId, request.categoryIds, request.selfDescription)
        return getPreferences(userId)
    }
}

class InvalidCategoryIdsException(ids: List<String>) :
    RuntimeException("Invalid category IDs: ${ids.joinToString(", ")}")
