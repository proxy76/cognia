package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ContentStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    PENDING_POST_REVIEW,
    DELETED
}
