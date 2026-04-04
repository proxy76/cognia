package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Follow(
    val followerId: String,
    val followedId: String,
    val createdAt: Instant
)
