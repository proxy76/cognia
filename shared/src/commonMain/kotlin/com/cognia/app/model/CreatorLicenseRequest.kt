package com.cognia.app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class CreatorLicenseRequest(
    val id: String,
    val creatorId: String,
    val status: LicenseRequestStatus,
    val moderatorId: String? = null,
    val rejectionReason: String? = null,
    val createdAt: Instant,
    val decidedAt: Instant? = null
)
