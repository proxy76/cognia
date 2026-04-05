package com.cognia.app.dto.feed

import kotlinx.serialization.Serializable

@Serializable
data class FeedItem(
    val id: String,
    val title: String,
    val creatorName: String,
    val creatorId: String,
    val thumbnailUrl: String? = null,
    val categoryName: String,
    val difficulty: String? = null,
    val hasQuiz: Boolean = false,
    val quizId: String? = null,
    val hasEli5: Boolean = false
)

@Serializable
data class FeedResponse(
    val items: List<FeedItem>,
    val page: Int,
    val hasMore: Boolean
)

@Serializable
data class TrackViewRequest(
    val contentId: String,
    val contentType: String = "VIDEO"
)

@Serializable
data class TrackShareRequest(
    val contentId: String,
    val contentType: String = "VIDEO"
)

@Serializable
data class TrackingResponse(
    val success: Boolean
)
