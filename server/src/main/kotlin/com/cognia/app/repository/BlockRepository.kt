package com.cognia.app.repository

import com.cognia.app.database.UserBlocksTable
import com.cognia.app.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class BlockRepository {

    data class BlockRow(
        val id: String,
        val blockerId: String,
        val blockedId: String,
        val createdAt: String
    )

    data class BlockedUserRow(
        val id: String,
        val blockedUserId: String,
        val blockedDisplayName: String,
        val blockedAvatarUrl: String?,
        val createdAt: String
    )

    fun block(blockerId: String, blockedId: String): BlockRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        UserBlocksTable.insert {
            it[UserBlocksTable.id] = id
            it[UserBlocksTable.blockerId] = blockerId
            it[UserBlocksTable.blockedId] = blockedId
            it[UserBlocksTable.createdAt] = now
        }

        BlockRow(id = id, blockerId = blockerId, blockedId = blockedId, createdAt = now)
    }

    fun unblock(blockerId: String, blockedId: String): Boolean = transaction {
        val deleted = UserBlocksTable.deleteWhere {
            (UserBlocksTable.blockerId eq blockerId) and (UserBlocksTable.blockedId eq blockedId)
        }
        deleted > 0
    }

    fun isBlocked(blockerId: String, blockedId: String): Boolean = transaction {
        UserBlocksTable.selectAll().where {
            (UserBlocksTable.blockerId eq blockerId) and (UserBlocksTable.blockedId eq blockedId)
        }.count() > 0
    }

    fun getBlockedUsers(blockerId: String): List<BlockedUserRow> = transaction {
        UserBlocksTable
            .join(UsersTable, JoinType.INNER, UserBlocksTable.blockedId, UsersTable.id)
            .selectAll()
            .where { UserBlocksTable.blockerId eq blockerId }
            .orderBy(UserBlocksTable.createdAt, SortOrder.DESC)
            .map { row ->
                BlockedUserRow(
                    id = row[UserBlocksTable.id],
                    blockedUserId = row[UserBlocksTable.blockedId],
                    blockedDisplayName = row[UsersTable.displayName],
                    blockedAvatarUrl = row[UsersTable.avatarUrl],
                    createdAt = row[UserBlocksTable.createdAt]
                )
            }
    }

    /**
     * Returns all user IDs that the given user has blocked.
     * Used for filtering feeds, search results, chat, etc.
     */
    fun getBlockedUserIds(blockerId: String): Set<String> = transaction {
        UserBlocksTable.selectAll()
            .where { UserBlocksTable.blockerId eq blockerId }
            .map { it[UserBlocksTable.blockedId] }
            .toSet()
    }
}
