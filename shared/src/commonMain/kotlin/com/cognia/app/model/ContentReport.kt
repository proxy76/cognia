package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class ContentReport(
    val id: String,
    val reporterId: String,
    val contentId: String,
    val contentType: ContentEntityType,
    val reason: ReportReason,
    val status: ReportStatus,
    val createdAt: Instant
)
