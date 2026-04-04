package com.cognia.app.repository

import com.cognia.app.database.ModerationAuditLogTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AuditLogRepository {

    data class AuditLogRow(
        val id: String,
        val moderatorId: String,
        val action: String,
        val targetType: String,
        val targetId: String,
        val details: String?,
        val createdAt: String
    )

    fun log(
        moderatorId: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String? = null
    ): AuditLogRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        ModerationAuditLogTable.insert {
            it[ModerationAuditLogTable.id] = id
            it[ModerationAuditLogTable.moderatorId] = moderatorId
            it[ModerationAuditLogTable.action] = action
            it[ModerationAuditLogTable.targetType] = targetType
            it[ModerationAuditLogTable.targetId] = targetId
            it[ModerationAuditLogTable.details] = details
            it[ModerationAuditLogTable.createdAt] = now
        }

        AuditLogRow(id, moderatorId, action, targetType, targetId, details, now)
    }

    fun getEntries(limit: Int = 100, offset: Long = 0): List<AuditLogRow> = transaction {
        ModerationAuditLogTable.selectAll()
            .orderBy(ModerationAuditLogTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toRow() }
    }

    fun getByModerator(moderatorId: String, limit: Int = 100): List<AuditLogRow> = transaction {
        ModerationAuditLogTable.selectAll()
            .where { ModerationAuditLogTable.moderatorId eq moderatorId }
            .orderBy(ModerationAuditLogTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { it.toRow() }
    }

    private fun ResultRow.toRow() = AuditLogRow(
        id = this[ModerationAuditLogTable.id],
        moderatorId = this[ModerationAuditLogTable.moderatorId],
        action = this[ModerationAuditLogTable.action],
        targetType = this[ModerationAuditLogTable.targetType],
        targetId = this[ModerationAuditLogTable.targetId],
        details = this[ModerationAuditLogTable.details],
        createdAt = this[ModerationAuditLogTable.createdAt]
    )
}
