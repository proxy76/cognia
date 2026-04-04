package com.cognia.app.repository

import com.cognia.app.database.ContentReportsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ReportRepository {

    data class ReportRow(
        val id: String,
        val reporterId: String,
        val contentId: String,
        val contentType: String,
        val reason: String,
        val status: String,
        val reportCount: Int,
        val aiAssessment: String?,
        val aiConfidence: String?,
        val resolution: String?,
        val resolvedAt: String?,
        val createdAt: String
    )

    /**
     * Creates a new report, or increments reportCount if a PENDING report already
     * exists for the same (contentId, contentType).
     */
    fun createReport(reporterId: String, contentId: String, contentType: String, reason: String): ReportRow = transaction {
        // Check for existing pending report on the same content
        val existing = ContentReportsTable.selectAll().where {
            (ContentReportsTable.contentId eq contentId) and
                (ContentReportsTable.contentType eq contentType) and
                (ContentReportsTable.status eq "PENDING")
        }.singleOrNull()

        if (existing != null) {
            val existingId = existing[ContentReportsTable.id]
            val newCount = existing[ContentReportsTable.reportCount] + 1
            ContentReportsTable.update({ ContentReportsTable.id eq existingId }) {
                it[reportCount] = newCount
            }
            findById(existingId)!!
        } else {
            val id = UUID.randomUUID().toString()
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

            ContentReportsTable.insert {
                it[ContentReportsTable.id] = id
                it[ContentReportsTable.reporterId] = reporterId
                it[ContentReportsTable.contentId] = contentId
                it[ContentReportsTable.contentType] = contentType
                it[ContentReportsTable.reason] = reason
                it[ContentReportsTable.status] = "PENDING"
                it[ContentReportsTable.reportCount] = 1
                it[ContentReportsTable.createdAt] = now
            }

            ReportRow(
                id = id, reporterId = reporterId, contentId = contentId,
                contentType = contentType, reason = reason, status = "PENDING",
                reportCount = 1, aiAssessment = null, aiConfidence = null,
                resolution = null, resolvedAt = null, createdAt = now
            )
        }
    }

    fun findById(id: String): ReportRow? = transaction {
        ContentReportsTable.selectAll().where { ContentReportsTable.id eq id }
            .map { it.toReportRow() }
            .singleOrNull()
    }

    fun getReports(status: String? = null): List<ReportRow> = transaction {
        val query = ContentReportsTable.selectAll()
        if (status != null) {
            query.where { ContentReportsTable.status eq status }
        }
        query.orderBy(ContentReportsTable.createdAt, SortOrder.DESC)
            .map { it.toReportRow() }
    }

    fun updateStatus(id: String, status: String): ReportRow? = transaction {
        val updated = ContentReportsTable.update({ ContentReportsTable.id eq id }) {
            it[ContentReportsTable.status] = status
        }
        if (updated > 0) findById(id) else null
    }

    fun resolve(id: String, status: String, resolution: String?): ReportRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        val updated = ContentReportsTable.update({ ContentReportsTable.id eq id }) {
            it[ContentReportsTable.status] = status
            it[ContentReportsTable.resolution] = resolution
            it[ContentReportsTable.resolvedAt] = now
        }
        if (updated > 0) findById(id) else null
    }

    fun updateAiAssessment(id: String, assessment: String, confidence: String): ReportRow? = transaction {
        val updated = ContentReportsTable.update({ ContentReportsTable.id eq id }) {
            it[ContentReportsTable.aiAssessment] = assessment
            it[ContentReportsTable.aiConfidence] = confidence
        }
        if (updated > 0) findById(id) else null
    }

    fun getReportStats(): ReportStats = transaction {
        val all = ContentReportsTable.selectAll().map { it[ContentReportsTable.status] to it[ContentReportsTable.contentType] }
        val byStatus = all.groupBy { it.first }.mapValues { it.value.size.toLong() }
        val byContentType = all.groupBy { it.second }.mapValues { it.value.size.toLong() }

        ReportStats(
            pending = byStatus["PENDING"] ?: 0,
            reviewed = byStatus["REVIEWED"] ?: 0,
            dismissed = byStatus["DISMISSED"] ?: 0,
            byContentType = byContentType
        )
    }

    data class ReportStats(
        val pending: Long,
        val reviewed: Long,
        val dismissed: Long,
        val byContentType: Map<String, Long>
    )

    private fun ResultRow.toReportRow() = ReportRow(
        id = this[ContentReportsTable.id],
        reporterId = this[ContentReportsTable.reporterId],
        contentId = this[ContentReportsTable.contentId],
        contentType = this[ContentReportsTable.contentType],
        reason = this[ContentReportsTable.reason],
        status = this[ContentReportsTable.status],
        reportCount = this[ContentReportsTable.reportCount],
        aiAssessment = this[ContentReportsTable.aiAssessment],
        aiConfidence = this[ContentReportsTable.aiConfidence],
        resolution = this[ContentReportsTable.resolution],
        resolvedAt = this[ContentReportsTable.resolvedAt],
        createdAt = this[ContentReportsTable.createdAt]
    )
}
