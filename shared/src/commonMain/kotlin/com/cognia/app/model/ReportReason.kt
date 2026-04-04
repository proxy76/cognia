package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReportReason {
    BAD_CONTENT,
    MISLEADING_CONTENT
}
