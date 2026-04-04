package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizOption(
    val id: String,
    val questionId: String,
    val text: String,
    val orderIndex: Int
)
