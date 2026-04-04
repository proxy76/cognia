package com.cognia.app.repository

import com.cognia.app.database.CreatorLicenseRequestsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class LicenseRepository {

    data class LicenseRequestRow(
        val id: String,
        val creatorId: String,
        val status: String,
        val moderatorId: String?,
        val rejectionReason: String?,
        val createdAt: String,
        val decidedAt: String?
    )

    fun createRequest(creatorId: String): LicenseRequestRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        CreatorLicenseRequestsTable.insert {
            it[CreatorLicenseRequestsTable.id] = id
            it[CreatorLicenseRequestsTable.creatorId] = creatorId
            it[CreatorLicenseRequestsTable.status] = "PENDING"
            it[CreatorLicenseRequestsTable.createdAt] = now
        }

        LicenseRequestRow(
            id = id,
            creatorId = creatorId,
            status = "PENDING",
            moderatorId = null,
            rejectionReason = null,
            createdAt = now,
            decidedAt = null
        )
    }

    fun findById(id: String): LicenseRequestRow? = transaction {
        CreatorLicenseRequestsTable.selectAll().where { CreatorLicenseRequestsTable.id eq id }
            .map { it.toRow() }
            .singleOrNull()
    }

    fun getRequestsByCreator(creatorId: String): List<LicenseRequestRow> = transaction {
        CreatorLicenseRequestsTable.selectAll()
            .where { CreatorLicenseRequestsTable.creatorId eq creatorId }
            .orderBy(CreatorLicenseRequestsTable.createdAt, SortOrder.DESC)
            .map { it.toRow() }
    }

    fun getPendingRequests(): List<LicenseRequestRow> = transaction {
        CreatorLicenseRequestsTable.selectAll()
            .where { CreatorLicenseRequestsTable.status eq "PENDING" }
            .orderBy(CreatorLicenseRequestsTable.createdAt, SortOrder.ASC)
            .map { it.toRow() }
    }

    fun getAllRequests(): List<LicenseRequestRow> = transaction {
        CreatorLicenseRequestsTable.selectAll()
            .orderBy(CreatorLicenseRequestsTable.createdAt, SortOrder.DESC)
            .map { it.toRow() }
    }

    fun updateStatus(id: String, status: String, moderatorId: String, rejectionReason: String? = null): LicenseRequestRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        val updated = CreatorLicenseRequestsTable.update({ CreatorLicenseRequestsTable.id eq id }) {
            it[CreatorLicenseRequestsTable.status] = status
            it[CreatorLicenseRequestsTable.moderatorId] = moderatorId
            it[CreatorLicenseRequestsTable.rejectionReason] = rejectionReason
            it[CreatorLicenseRequestsTable.decidedAt] = now
        }

        if (updated > 0) findById(id) else null
    }

    fun revokePendingRequests(creatorId: String): Int = transaction {
        CreatorLicenseRequestsTable.update({
            (CreatorLicenseRequestsTable.creatorId eq creatorId) and
                (CreatorLicenseRequestsTable.status eq "PENDING")
        }) {
            it[CreatorLicenseRequestsTable.status] = "REVOKED"
        }
    }

    private fun ResultRow.toRow() = LicenseRequestRow(
        id = this[CreatorLicenseRequestsTable.id],
        creatorId = this[CreatorLicenseRequestsTable.creatorId],
        status = this[CreatorLicenseRequestsTable.status],
        moderatorId = this[CreatorLicenseRequestsTable.moderatorId],
        rejectionReason = this[CreatorLicenseRequestsTable.rejectionReason],
        createdAt = this[CreatorLicenseRequestsTable.createdAt],
        decidedAt = this[CreatorLicenseRequestsTable.decidedAt]
    )
}
