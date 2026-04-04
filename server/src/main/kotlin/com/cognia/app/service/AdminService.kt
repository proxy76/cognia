package com.cognia.app.service

import com.cognia.app.database.UsersTable
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class AdminService(
    private val auditLogService: AuditLogService
) {

    fun suspendUser(userId: String, moderatorId: String, durationDays: Int, reason: String) {
        val user = transaction {
            UsersTable.selectAll().where { UsersTable.id eq userId }.singleOrNull()
        } ?: throw UserNotFoundException(userId)

        val suspendedUntil = Clock.System.now()
            .plus(durationDays, DateTimeUnit.DAY, TimeZone.UTC)
            .toLocalDateTime(TimeZone.UTC).toString()

        transaction {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.suspendedUntil] = suspendedUntil
            }
        }

        auditLogService.log(
            moderatorId = moderatorId,
            action = "SUSPEND_USER",
            targetType = "USER",
            targetId = userId,
            details = "Duration: ${durationDays}d, Reason: $reason, Until: $suspendedUntil"
        )
    }

    fun unsuspendUser(userId: String, moderatorId: String) {
        transaction {
            UsersTable.update({ UsersTable.id eq userId }) {
                it[UsersTable.suspendedUntil] = null
            }
        }

        auditLogService.log(
            moderatorId = moderatorId,
            action = "UNSUSPEND_USER",
            targetType = "USER",
            targetId = userId,
            details = null
        )
    }

    fun isUserSuspended(userId: String): Boolean {
        val suspendedUntil = transaction {
            UsersTable.selectAll().where { UsersTable.id eq userId }
                .singleOrNull()?.get(UsersTable.suspendedUntil)
        } ?: return false

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        return suspendedUntil > now
    }
}
