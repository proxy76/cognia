package com.cognia.app.service

import com.cognia.app.database.UsersTable
import com.cognia.app.dto.moderation.StrikeResponse
import com.cognia.app.repository.LicenseRepository
import com.cognia.app.repository.StrikeRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class StrikeService(
    private val strikeRepository: StrikeRepository,
    private val licenseRepository: LicenseRepository
) {

    companion object {
        const val COOLDOWN_DAYS = 7
    }

    fun issueStrike(userId: String, moderatorId: String, reason: String): StrikeResponse {
        val now = Clock.System.now()
        val cooldownUntil = now.plus(COOLDOWN_DAYS, DateTimeUnit.DAY, TimeZone.UTC)
            .toLocalDateTime(TimeZone.UTC).toString()

        val strike = strikeRepository.createStrike(userId, moderatorId, reason, cooldownUntil)

        // If user is LICENSED_CREATOR, revoke license (change role to REGULAR_CREATOR)
        val userRole = transaction {
            UsersTable.selectAll().where { UsersTable.id eq userId }
                .singleOrNull()?.get(UsersTable.role)
        }

        if (userRole == "LICENSED_CREATOR") {
            transaction {
                UsersTable.update({ UsersTable.id eq userId }) {
                    it[role] = "REGULAR_CREATOR"
                }
            }
        }

        // Revoke any pending license requests
        licenseRepository.revokePendingRequests(userId)

        return strike.toResponse()
    }

    fun isInCooldown(userId: String): Boolean {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        return strikeRepository.getActiveStrikes(userId, now).isNotEmpty()
    }

    fun getStrikesByUserId(userId: String): List<StrikeResponse> {
        return strikeRepository.getStrikesByUserId(userId).map { it.toResponse() }
    }

    private fun StrikeRepository.StrikeRow.toResponse() = StrikeResponse(
        id = id,
        userId = userId,
        moderatorId = moderatorId,
        reason = reason,
        cooldownUntil = cooldownUntil,
        createdAt = createdAt
    )
}
