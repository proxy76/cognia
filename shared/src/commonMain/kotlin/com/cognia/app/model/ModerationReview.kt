package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ModerationReview(
    val id: String,
    val contentId: String,
    val contentType: ContentEntityType,
    val moderatorId: String? = null,
    val decision: ModerationDecision? = null,
    val reason: String? = null,
    val isPostPublication: Boolean,
    val createdAt: Instant,
    val decidedAt: Instant? = null
)
