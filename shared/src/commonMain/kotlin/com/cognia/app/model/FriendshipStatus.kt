package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}
