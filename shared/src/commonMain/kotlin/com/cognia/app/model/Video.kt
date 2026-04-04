package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Video(
    val id: String,
    val creatorId: String,
    val title: String,
    val description: String? = null,
    val categoryId: String,
    val videoUrl: String? = null,
    val thumbnailUrl: String? = null,
    val status: ContentStatus,
    val difficulty: Difficulty? = null,
    val createdAt: Instant,
    val publishedAt: Instant? = null
)
