package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class UserBadge(
    val userId: String,
    val badgeId: String,
    val awardedAt: Instant
)
