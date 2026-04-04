package com.cognia.app.dto.feed

import com.cognia.app.dto.content.VideoResponse
import kotlinx.serialization.Serializable

@Serializable
data class FeedItemResponse(
    val type: String,
    val video: VideoResponse
)

@Serializable
data class FeedResponse(
    val items: List<FeedItemResponse>,
    val nextCursor: String? = null
)
