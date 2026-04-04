package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Quiz(
    val id: String,
    val creatorId: String,
    val videoId: String? = null,
    val title: String,
    val quizType: QuizType,
    val categoryId: String,
    val status: ContentStatus,
    val difficulty: Difficulty? = null,
    val createdAt: Instant
)
