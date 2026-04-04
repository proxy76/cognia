package com.cognia.app.service

import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.category.CreateCategoryRequest
import com.cognia.app.dto.category.UpdateCategoryRequest
import com.cognia.app.repository.CategoryRepository

class CategoryService(
    private val categoryRepository: CategoryRepository
) {

    fun findAll(): List<CategoryResponse> {
        return categoryRepository.findAll().map { it.toResponse() }
    }

    fun findById(id: String): CategoryResponse? {
        return categoryRepository.findById(id)?.toResponse()
    }

    fun create(request: CreateCategoryRequest): CategoryResponse {
        require(request.name.isNotBlank()) { "Category name must not be blank" }
        require(request.slug.isNotBlank()) { "Category slug must not be blank" }

        if (categoryRepository.findByName(request.name) != null) {
            throw CategoryAlreadyExistsException("Category with name '${request.name}' already exists")
        }
        if (categoryRepository.findBySlug(request.slug) != null) {
            throw CategoryAlreadyExistsException("Category with slug '${request.slug}' already exists")
        }

        return categoryRepository.create(request.name, request.slug).toResponse()
    }

    fun update(id: String, request: UpdateCategoryRequest): CategoryResponse {
        require(request.name.isNotBlank()) { "Category name must not be blank" }
        require(request.slug.isNotBlank()) { "Category slug must not be blank" }

        // Check for existing category with same name (excluding current)
        val existingByName = categoryRepository.findByName(request.name)
        if (existingByName != null && existingByName.id != id) {
            throw CategoryAlreadyExistsException("Category with name '${request.name}' already exists")
        }

        val existingBySlug = categoryRepository.findBySlug(request.slug)
        if (existingBySlug != null && existingBySlug.id != id) {
            throw CategoryAlreadyExistsException("Category with slug '${request.slug}' already exists")
        }

        return categoryRepository.update(id, request.name, request.slug)?.toResponse()
            ?: throw CategoryNotFoundException(id)
    }

    fun delete(id: String): Boolean {
        if (categoryRepository.findById(id) == null) {
            throw CategoryNotFoundException(id)
        }
        return categoryRepository.delete(id)
    }

    fun seedCategories() {
        if (categoryRepository.count() > 0) return

        val seedData = listOf(
            "Science" to "science",
            "Mathematics" to "mathematics",
            "History" to "history",
            "Literature" to "literature",
            "Technology" to "technology",
            "Art" to "art",
            "Music" to "music",
            "Languages" to "languages",
            "Philosophy" to "philosophy",
            "Psychology" to "psychology",
            "Computer Science" to "computer-science",
            "Biology" to "biology",
            "Chemistry" to "chemistry",
            "Physics" to "physics",
            "Economics" to "economics",
            "Business" to "business",
            "Health" to "health",
            "Geography" to "geography",
            "Engineering" to "engineering",
            "Environment" to "environment"
        )

        for ((name, slug) in seedData) {
            categoryRepository.create(name, slug)
        }
    }

    private fun com.cognia.app.repository.CategoryRow.toResponse() =
        CategoryResponse(id = id, name = name, slug = slug)
}

class CategoryAlreadyExistsException(message: String) : RuntimeException(message)
class CategoryNotFoundException(id: String) : RuntimeException("Category not found: $id")
