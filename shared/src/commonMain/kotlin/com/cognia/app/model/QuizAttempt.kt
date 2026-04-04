package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class QuizAttempt(
    val id: String,
    val userId: String,
    val quizId: String,
    val score: Int,
    val totalQuestions: Int,
    val pointsAwarded: Long,
    val completedAt: Instant
)
