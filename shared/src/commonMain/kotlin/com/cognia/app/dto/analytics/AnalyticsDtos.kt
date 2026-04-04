package com.cognia.app.dto.analytics

import kotlinx.serialization.Serializable

@Serializable
data class CreatorAnalyticsResponse(
    val totalViews: Long,
    val totalQuizAttempts: Long,
    val averageQuizScore: Double,
    val videoStats: List<VideoStatsResponse>
)

@Serializable
data class VideoStatsResponse(
    val videoId: String,
    val title: String,
    val viewCount: Long
)
