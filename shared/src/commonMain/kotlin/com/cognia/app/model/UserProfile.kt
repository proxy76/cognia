package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val userId: String,
    val selfDescription: String? = null,
    val categories: List<String> = emptyList(),
    val level: Int = 1,
    val totalPoints: Long = 0
)
