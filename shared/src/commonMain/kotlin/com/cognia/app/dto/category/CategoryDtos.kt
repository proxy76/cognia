package com.cognia.app.dto.category

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(val id: String, val name: String, val slug: String)

@Serializable
data class CreateCategoryRequest(val name: String, val slug: String)

@Serializable
data class UpdateCategoryRequest(val name: String, val slug: String)
