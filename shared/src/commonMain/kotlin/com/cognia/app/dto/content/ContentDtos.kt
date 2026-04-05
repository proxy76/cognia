package com.cognia.app.dto.content

import com.cognia.app.dto.user.CategorySummary
import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class VideoResponse(
    val id: String,
    val creator: UserSummary,
    val title: String,
    val description: String? = null,
    val category: CategorySummary,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val difficulty: String? = null,
    val publishedAt: String? = null,
    val viewCount: Long = 0,
    val quizId: String? = null
)

@Serializable
data class VideoCreateResponse(
    val id: String,
    val status: String,
    val title: String,
    val createdAt: String
)

@Serializable
data class VideoUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val categoryId: String? = null,
    val difficulty: String? = null
)

@Serializable
data class VideoStatusResponse(
    val id: String,
    val status: String
)

@Serializable
data class VideoUploadResponse(
    val id: String,
    val status: String,
    val message: String
)

@Serializable
data class VideoCreateDraftRequest(
    val title: String,
    val description: String? = null,
    val categoryId: String,
    val difficulty: String? = null
)

@Serializable
data class VideoDetailResponse(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String? = null,
    val categoryId: String,
    val videoUrl: String? = null,
    val eli5VideoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val status: String,
    val difficulty: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val publishedAt: String? = null
)
