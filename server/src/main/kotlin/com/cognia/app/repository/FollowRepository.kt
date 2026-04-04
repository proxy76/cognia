package com.cognia.app.repository

import com.cognia.app.database.FollowsTable
import com.cognia.app.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FollowRepository {

    data class FollowRow(
        val followerId: String,
        val followedId: String,
        val createdAt: String
    )

    data class UserSummaryRow(
        val id: String,
        val displayName: String,
        val avatarUrl: String?
    )

    fun follow(followerId: String, followedId: String): Unit = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        FollowsTable.insert {
            it[FollowsTable.followerId] = followerId
            it[FollowsTable.followedId] = followedId
            it[FollowsTable.createdAt] = now
        }
    }

    fun unfollow(followerId: String, followedId: String): Boolean = transaction {
        val deleted = FollowsTable.deleteWhere {
            (FollowsTable.followerId eq followerId) and (FollowsTable.followedId eq followedId)
        }
        deleted > 0
    }

    fun isFollowing(followerId: String, followedId: String): Boolean = transaction {
        FollowsTable.selectAll().where {
            (FollowsTable.followerId eq followerId) and (FollowsTable.followedId eq followedId)
        }.count() > 0
    }

    fun getFollowers(userId: String): List<UserSummaryRow> = transaction {
        FollowsTable
            .join(UsersTable, JoinType.INNER, FollowsTable.followerId, UsersTable.id)
            .select(UsersTable.id, UsersTable.displayName, UsersTable.avatarUrl)
            .where { FollowsTable.followedId eq userId }
            .orderBy(FollowsTable.createdAt, SortOrder.DESC)
            .map {
                UserSummaryRow(
                    id = it[UsersTable.id],
                    displayName = it[UsersTable.displayName],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }
    }

    fun getFollowing(userId: String): List<UserSummaryRow> = transaction {
        FollowsTable
            .join(UsersTable, JoinType.INNER, FollowsTable.followedId, UsersTable.id)
            .select(UsersTable.id, UsersTable.displayName, UsersTable.avatarUrl)
            .where { FollowsTable.followerId eq userId }
            .orderBy(FollowsTable.createdAt, SortOrder.DESC)
            .map {
                UserSummaryRow(
                    id = it[UsersTable.id],
                    displayName = it[UsersTable.displayName],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }
    }
}
