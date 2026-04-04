package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class ReportStatus {
    PENDING,
    REVIEWED,
    DISMISSED
}
