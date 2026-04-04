package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ContentStatus {
    DRAFT,
    PENDING_REVIEW,
    APPROVED,
    REJECTED,
    PUBLISHED,
    ARCHIVED,
    PENDING_POST_REVIEW,
    DELETED
}
