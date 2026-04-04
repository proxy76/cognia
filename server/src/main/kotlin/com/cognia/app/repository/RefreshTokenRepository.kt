package com.cognia.app.repository

import com.cognia.app.database.RefreshTokensTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class RefreshTokenRepository {

    data class RefreshTokenRow(
        val id: String,
        val userId: String,
        val tokenHash: String,
        val expiresAt: String,
        val createdAt: String
    )

    fun save(userId: String, tokenHash: String, expiresAt: String): String = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        RefreshTokensTable.insert {
            it[RefreshTokensTable.id] = id
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.tokenHash] = tokenHash
            it[RefreshTokensTable.expiresAt] = expiresAt
            it[RefreshTokensTable.createdAt] = now
        }
        id
    }

    fun findByUserId(userId: String): List<RefreshTokenRow> = transaction {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.userId eq userId }
            .map { it.toRefreshTokenRow() }
    }

    fun deleteByTokenHash(tokenHash: String): Boolean = transaction {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.tokenHash eq tokenHash } > 0
    }

    fun findAll(): List<RefreshTokenRow> = transaction {
        RefreshTokensTable.selectAll()
            .map { it.toRefreshTokenRow() }
    }

    fun deleteAllForUser(userId: String): Int = transaction {
        RefreshTokensTable.deleteWhere { RefreshTokensTable.userId eq userId }
    }

    private fun ResultRow.toRefreshTokenRow() = RefreshTokenRow(
        id = this[RefreshTokensTable.id],
        userId = this[RefreshTokensTable.userId],
        tokenHash = this[RefreshTokensTable.tokenHash],
        expiresAt = this[RefreshTokensTable.expiresAt],
        createdAt = this[RefreshTokensTable.createdAt]
    )
}
