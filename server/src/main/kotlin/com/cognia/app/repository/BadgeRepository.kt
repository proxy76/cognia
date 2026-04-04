package com.cognia.app.repository

import com.cognia.app.database.BadgesTable
import com.cognia.app.database.UserBadgesTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

data class BadgeRow(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val criteria: String
)

data class UserBadgeRow(
    val userId: String,
    val badgeId: String,
    val awardedAt: String,
    val badge: BadgeRow
)

class BadgeRepository {

    fun findAll(): List<BadgeRow> = transaction {
        BadgesTable.selectAll().map { it.toBadgeRow() }
    }

    fun findById(id: String): BadgeRow? = transaction {
        BadgesTable.selectAll()
            .where { BadgesTable.id eq id }
            .singleOrNull()?.toBadgeRow()
    }

    /**
     * Awards a badge to a user. Returns false if the user already has this badge.
     */
    fun awardBadge(userId: String, badgeId: String): Boolean = transaction {
        val exists = UserBadgesTable.selectAll()
            .where { (UserBadgesTable.userId eq userId) and (UserBadgesTable.badgeId eq badgeId) }
            .count() > 0

        if (exists) return@transaction false

        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        UserBadgesTable.insert {
            it[UserBadgesTable.userId] = userId
            it[UserBadgesTable.badgeId] = badgeId
            it[UserBadgesTable.awardedAt] = now
        }
        true
    }

    fun getUserBadges(userId: String): List<UserBadgeRow> = transaction {
        UserBadgesTable
            .join(BadgesTable, JoinType.INNER, UserBadgesTable.badgeId, BadgesTable.id)
            .selectAll()
            .where { UserBadgesTable.userId eq userId }
            .map {
                UserBadgeRow(
                    userId = it[UserBadgesTable.userId],
                    badgeId = it[UserBadgesTable.badgeId],
                    awardedAt = it[UserBadgesTable.awardedAt],
                    badge = it.toBadgeRow()
                )
            }
    }

    fun hasUserBadge(userId: String, badgeId: String): Boolean = transaction {
        UserBadgesTable.selectAll()
            .where { (UserBadgesTable.userId eq userId) and (UserBadgesTable.badgeId eq badgeId) }
            .count() > 0
    }

    fun seedBadge(id: String, name: String, description: String, iconUrl: String, criteria: String): Unit = transaction {
        val exists = BadgesTable.selectAll()
            .where { BadgesTable.id eq id }
            .count() > 0

        if (!exists) {
            BadgesTable.insert {
                it[BadgesTable.id] = id
                it[BadgesTable.name] = name
                it[BadgesTable.description] = description
                it[BadgesTable.iconUrl] = iconUrl
                it[BadgesTable.criteria] = criteria
            }
        }
    }

    private fun ResultRow.toBadgeRow() = BadgeRow(
        id = this[BadgesTable.id],
        name = this[BadgesTable.name],
        description = this[BadgesTable.description],
        iconUrl = this[BadgesTable.iconUrl],
        criteria = this[BadgesTable.criteria]
    )
}
