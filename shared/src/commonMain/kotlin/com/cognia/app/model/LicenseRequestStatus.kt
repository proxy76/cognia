package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class LicenseRequestStatus {
    PENDING,
    APPROVED,
    REJECTED
}
