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
        val createdAt: String
    )

    fun createReport(reporterId: String, contentId: String, contentType: String, reason: String): ReportRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        ContentReportsTable.insert {
            it[ContentReportsTable.id] = id
            it[ContentReportsTable.reporterId] = reporterId
            it[ContentReportsTable.contentId] = contentId
            it[ContentReportsTable.contentType] = contentType
            it[ContentReportsTable.reason] = reason
            it[ContentReportsTable.status] = "PENDING"
            it[ContentReportsTable.createdAt] = now
        }

        ReportRow(
            id = id,
            reporterId = reporterId,
            contentId = contentId,
            contentType = contentType,
            reason = reason,
            status = "PENDING",
            createdAt = now
        )
    }

    fun findById(id: String): ReportRow? = transaction {
        ContentReportsTable.selectAll().where { ContentReportsTable.id eq id }
            .map { it.toReportRow() }
            .singleOrNull()
    }

    fun getReports(): List<ReportRow> = transaction {
        ContentReportsTable.selectAll()
            .orderBy(ContentReportsTable.createdAt, SortOrder.DESC)
            .map { it.toReportRow() }
    }

    fun updateStatus(id: String, status: String): ReportRow? = transaction {
        val updated = ContentReportsTable.update({ ContentReportsTable.id eq id }) {
            it[ContentReportsTable.status] = status
        }

        if (updated > 0) findById(id) else null
    }

    private fun ResultRow.toReportRow() = ReportRow(
        id = this[ContentReportsTable.id],
        reporterId = this[ContentReportsTable.reporterId],
        contentId = this[ContentReportsTable.contentId],
        contentType = this[ContentReportsTable.contentType],
        reason = this[ContentReportsTable.reason],
        status = this[ContentReportsTable.status],
        createdAt = this[ContentReportsTable.createdAt]
    )
}
