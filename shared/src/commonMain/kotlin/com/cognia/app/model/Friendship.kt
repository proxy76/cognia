package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Friendship(
    val id: String,
    val requesterId: String,
    val receiverId: String,
    val status: FriendshipStatus,
    val createdAt: Instant,
    val acceptedAt: Instant? = null
)
