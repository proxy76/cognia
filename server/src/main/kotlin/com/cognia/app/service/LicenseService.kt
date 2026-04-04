package com.cognia.app.service

import com.cognia.app.database.UsersTable
import com.cognia.app.dto.moderation.LicenseRequestResponse
import com.cognia.app.repository.LicenseRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class LicenseService(
    private val licenseRepository: LicenseRepository,
    private val strikeService: StrikeService
) {

    fun requestLicense(creatorId: String, creatorRole: String): LicenseRequestResponse {
        if (creatorRole != "REGULAR_CREATOR") {
            throw LicenseRequestDeniedException("Only REGULAR_CREATOR users can request a license")
        }

        if (strikeService.isInCooldown(creatorId)) {
            throw LicenseRequestDeniedException("Cannot request license while in strike cooldown")
        }

        // Check for existing pending request
        val pendingRequests = licenseRepository.getRequestsByCreator(creatorId)
            .filter { it.status == "PENDING" }
        if (pendingRequests.isNotEmpty()) {
            throw LicenseRequestDeniedException("You already have a pending license request")
        }

        val request = licenseRepository.createRequest(creatorId)
        return request.toResponse()
    }

    fun approveRequest(requestId: String, moderatorId: String): LicenseRequestResponse {
        val request = licenseRepository.findById(requestId)
            ?: throw LicenseRequestNotFoundException("License request not found: $requestId")

        if (request.status != "PENDING") {
            throw LicenseRequestDeniedException("Request is not pending (current status: ${request.status})")
        }

        // Update user role to LICENSED_CREATOR
        transaction {
            UsersTable.update({ UsersTable.id eq request.creatorId }) {
                it[role] = "LICENSED_CREATOR"
            }
        }

        val updated = licenseRepository.updateStatus(requestId, "APPROVED", moderatorId)
            ?: throw LicenseRequestNotFoundException("License request not found: $requestId")

        return updated.toResponse()
    }

    fun rejectRequest(requestId: String, moderatorId: String, reason: String?): LicenseRequestResponse {
        val request = licenseRepository.findById(requestId)
            ?: throw LicenseRequestNotFoundException("License request not found: $requestId")

        if (request.status != "PENDING") {
            throw LicenseRequestDeniedException("Request is not pending (current status: ${request.status})")
        }

        val updated = licenseRepository.updateStatus(requestId, "REJECTED", moderatorId, reason)
            ?: throw LicenseRequestNotFoundException("License request not found: $requestId")

        return updated.toResponse()
    }

    fun getRequests(): List<LicenseRequestResponse> {
        return licenseRepository.getAllRequests().map { it.toResponse() }
    }

    fun getRequestById(requestId: String): LicenseRequestResponse? {
        return licenseRepository.findById(requestId)?.toResponse()
    }

    private fun LicenseRepository.LicenseRequestRow.toResponse() = LicenseRequestResponse(
        id = id,
        creatorId = creatorId,
        status = status,
        moderatorId = moderatorId,
        rejectionReason = rejectionReason,
        createdAt = createdAt,
        decidedAt = decidedAt
    )
}

class LicenseRequestNotFoundException(message: String) : RuntimeException(message)
class LicenseRequestDeniedException(message: String) : RuntimeException(message)
