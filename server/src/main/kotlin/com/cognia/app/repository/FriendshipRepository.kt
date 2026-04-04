package com.cognia.app.repository

import com.cognia.app.database.FriendshipsTable
import com.cognia.app.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class FriendshipRepository {

    data class FriendshipRow(
        val id: String,
        val requesterId: String,
        val receiverId: String,
        val status: String,
        val createdAt: String,
        val acceptedAt: String?
    )

    data class FriendshipWithUsersRow(
        val id: String,
        val requesterId: String,
        val requesterDisplayName: String,
        val requesterAvatarUrl: String?,
        val receiverId: String,
        val receiverDisplayName: String,
        val receiverAvatarUrl: String?,
        val status: String,
        val createdAt: String
    )

    fun findById(id: String): FriendshipRow? = transaction {
        FriendshipsTable.selectAll().where { FriendshipsTable.id eq id }
            .map { it.toFriendshipRow() }
            .singleOrNull()
    }

    fun findExisting(userA: String, userB: String): FriendshipRow? = transaction {
        FriendshipsTable.selectAll().where {
            ((FriendshipsTable.requesterId eq userA) and (FriendshipsTable.receiverId eq userB)) or
            ((FriendshipsTable.requesterId eq userB) and (FriendshipsTable.receiverId eq userA))
        }.map { it.toFriendshipRow() }.singleOrNull()
    }

    fun sendRequest(requesterId: String, receiverId: String): FriendshipRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        FriendshipsTable.insert {
            it[FriendshipsTable.id] = id
            it[FriendshipsTable.requesterId] = requesterId
            it[FriendshipsTable.receiverId] = receiverId
            it[FriendshipsTable.status] = "PENDING"
            it[FriendshipsTable.createdAt] = now
        }
        FriendshipRow(id, requesterId, receiverId, "PENDING", now, null)
    }

    fun acceptRequest(id: String): Unit = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        FriendshipsTable.update({ FriendshipsTable.id eq id }) {
            it[status] = "ACCEPTED"
            it[acceptedAt] = now
        }
    }

    fun declineRequest(id: String): Unit = transaction {
        FriendshipsTable.update({ FriendshipsTable.id eq id }) {
            it[status] = "DECLINED"
        }
    }

    fun getPendingRequests(userId: String): List<FriendshipWithUsersRow> = transaction {
        val requesterTable = UsersTable.alias("requester")
        val receiverTable = UsersTable.alias("receiver")

        FriendshipsTable
            .join(requesterTable, JoinType.INNER, FriendshipsTable.requesterId, requesterTable[UsersTable.id])
            .join(receiverTable, JoinType.INNER, FriendshipsTable.receiverId, receiverTable[UsersTable.id])
            .select(
                FriendshipsTable.id,
                FriendshipsTable.requesterId,
                requesterTable[UsersTable.displayName],
                requesterTable[UsersTable.avatarUrl],
                FriendshipsTable.receiverId,
                receiverTable[UsersTable.displayName],
                receiverTable[UsersTable.avatarUrl],
                FriendshipsTable.status,
                FriendshipsTable.createdAt
            )
            .where {
                (FriendshipsTable.receiverId eq userId) and (FriendshipsTable.status eq "PENDING")
            }
            .orderBy(FriendshipsTable.createdAt, SortOrder.DESC)
            .map {
                FriendshipWithUsersRow(
                    id = it[FriendshipsTable.id],
                    requesterId = it[FriendshipsTable.requesterId],
                    requesterDisplayName = it[requesterTable[UsersTable.displayName]],
                    requesterAvatarUrl = it[requesterTable[UsersTable.avatarUrl]],
                    receiverId = it[FriendshipsTable.receiverId],
                    receiverDisplayName = it[receiverTable[UsersTable.displayName]],
                    receiverAvatarUrl = it[receiverTable[UsersTable.avatarUrl]],
                    status = it[FriendshipsTable.status],
                    createdAt = it[FriendshipsTable.createdAt]
                )
            }
    }

    fun getFriends(userId: String): List<FollowRepository.UserSummaryRow> = transaction {
        // Friends are where the user is either requester or receiver and status is ACCEPTED
        val friendships = FriendshipsTable.selectAll().where {
            ((FriendshipsTable.requesterId eq userId) or (FriendshipsTable.receiverId eq userId)) and
            (FriendshipsTable.status eq "ACCEPTED")
        }.map { it.toFriendshipRow() }

        val friendIds = friendships.map { row ->
            if (row.requesterId == userId) row.receiverId else row.requesterId
        }

        if (friendIds.isEmpty()) return@transaction emptyList()

        UsersTable.selectAll().where { UsersTable.id inList friendIds }
            .map {
                FollowRepository.UserSummaryRow(
                    id = it[UsersTable.id],
                    displayName = it[UsersTable.displayName],
                    avatarUrl = it[UsersTable.avatarUrl]
                )
            }
    }

    fun areFriends(userA: String, userB: String): Boolean = transaction {
        FriendshipsTable.selectAll().where {
            (((FriendshipsTable.requesterId eq userA) and (FriendshipsTable.receiverId eq userB)) or
            ((FriendshipsTable.requesterId eq userB) and (FriendshipsTable.receiverId eq userA))) and
            (FriendshipsTable.status eq "ACCEPTED")
        }.count() > 0
    }

    private fun ResultRow.toFriendshipRow() = FriendshipRow(
        id = this[FriendshipsTable.id],
        requesterId = this[FriendshipsTable.requesterId],
        receiverId = this[FriendshipsTable.receiverId],
        status = this[FriendshipsTable.status],
        createdAt = this[FriendshipsTable.createdAt],
        acceptedAt = this[FriendshipsTable.acceptedAt]
    )
}
