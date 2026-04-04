package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class NotificationType {
    FRIEND_REQUEST_RECEIVED,
    FRIEND_REQUEST_ACCEPTED,
    CONTENT_APPROVED,
    CONTENT_REJECTED,
    BADGE_EARNED,
    LEVEL_UP,
    NEW_FOLLOWER,
    STRIKE_ISSUED,
    LICENSE_APPROVED,
    LICENSE_REJECTED,
    LICENSE_REVOKED
}
