package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: UserRole,
    val createdAt: Instant,
    val updatedAt: Instant
)
