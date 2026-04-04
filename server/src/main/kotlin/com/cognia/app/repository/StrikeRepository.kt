package com.cognia.app.repository

import com.cognia.app.database.StrikesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class StrikeRepository {

    data class StrikeRow(
        val id: String,
        val userId: String,
        val moderatorId: String,
        val reason: String,
        val cooldownUntil: String,
        val createdAt: String
    )

    fun createStrike(userId: String, moderatorId: String, reason: String, cooldownUntil: String): StrikeRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        StrikesTable.insert {
            it[StrikesTable.id] = id
            it[StrikesTable.userId] = userId
            it[StrikesTable.moderatorId] = moderatorId
            it[StrikesTable.reason] = reason
            it[StrikesTable.cooldownUntil] = cooldownUntil
            it[StrikesTable.createdAt] = now
        }

        StrikeRow(
            id = id,
            userId = userId,
            moderatorId = moderatorId,
            reason = reason,
            cooldownUntil = cooldownUntil,
            createdAt = now
        )
    }

    fun getStrikesByUserId(userId: String): List<StrikeRow> = transaction {
        StrikesTable.selectAll()
            .where { StrikesTable.userId eq userId }
            .orderBy(StrikesTable.createdAt, SortOrder.DESC)
            .map { it.toStrikeRow() }
    }

    fun getActiveStrikes(userId: String, now: String): List<StrikeRow> = transaction {
        StrikesTable.selectAll()
            .where { (StrikesTable.userId eq userId) and (StrikesTable.cooldownUntil greater now) }
            .map { it.toStrikeRow() }
    }

    private fun ResultRow.toStrikeRow() = StrikeRow(
        id = this[StrikesTable.id],
        userId = this[StrikesTable.userId],
        moderatorId = this[StrikesTable.moderatorId],
        reason = this[StrikesTable.reason],
        cooldownUntil = this[StrikesTable.cooldownUntil],
        createdAt = this[StrikesTable.createdAt]
    )
}
