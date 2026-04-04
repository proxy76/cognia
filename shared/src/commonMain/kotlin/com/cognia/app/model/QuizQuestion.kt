package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizQuestion(
    val id: String,
    val quizId: String,
    val questionText: String,
    val correctOptionIndex: Int,
    val orderIndex: Int,
    val options: List<QuizOption>
)
