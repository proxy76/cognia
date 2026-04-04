package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    LEARNER,
    REGULAR_CREATOR,
    LICENSED_CREATOR,
    MODERATOR,
    ADMIN
}
