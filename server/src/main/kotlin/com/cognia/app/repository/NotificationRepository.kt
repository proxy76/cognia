package com.cognia.app.repository

import com.cognia.app.database.NotificationsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class NotificationRepository {

    data class NotificationRow(
        val id: String,
        val userId: String,
        val type: String,
        val title: String,
        val body: String,
        val referenceId: String?,
        val referenceType: String?,
        val read: Boolean,
        val createdAt: String
    )

    fun create(
        userId: String,
        type: String,
        title: String,
        body: String,
        referenceId: String? = null,
        referenceType: String? = null
    ): NotificationRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        NotificationsTable.insert {
            it[NotificationsTable.id] = id
            it[NotificationsTable.userId] = userId
            it[NotificationsTable.type] = type
            it[NotificationsTable.title] = title
            it[NotificationsTable.body] = body
            it[NotificationsTable.referenceId] = referenceId
            it[NotificationsTable.referenceType] = referenceType
            it[NotificationsTable.read] = false
            it[NotificationsTable.createdAt] = now
        }

        NotificationRow(id, userId, type, title, body, referenceId, referenceType, false, now)
    }

    fun findByUserId(userId: String, page: Int, limit: Int): List<NotificationRow> = transaction {
        val offset = ((page - 1) * limit).toLong()
        NotificationsTable.selectAll()
            .where { NotificationsTable.userId eq userId }
            .orderBy(NotificationsTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toNotificationRow() }
    }

    fun countByUserId(userId: String): Int = transaction {
        NotificationsTable.selectAll()
            .where { NotificationsTable.userId eq userId }
            .count()
            .toInt()
    }

    fun getUnreadCount(userId: String): Int = transaction {
        NotificationsTable.selectAll()
            .where { (NotificationsTable.userId eq userId) and (NotificationsTable.read eq false) }
            .count()
            .toInt()
    }

    fun markAsRead(notificationId: String, userId: String): Boolean = transaction {
        val updated = NotificationsTable.update(
            where = { (NotificationsTable.id eq notificationId) and (NotificationsTable.userId eq userId) }
        ) {
            it[read] = true
        }
        updated > 0
    }

    fun markAllAsRead(userId: String): Int = transaction {
        NotificationsTable.update(
            where = { (NotificationsTable.userId eq userId) and (NotificationsTable.read eq false) }
        ) {
            it[read] = true
        }
    }

    fun findById(notificationId: String): NotificationRow? = transaction {
        NotificationsTable.selectAll()
            .where { NotificationsTable.id eq notificationId }
            .map { it.toNotificationRow() }
            .singleOrNull()
    }

    private fun ResultRow.toNotificationRow() = NotificationRow(
        id = this[NotificationsTable.id],
        userId = this[NotificationsTable.userId],
        type = this[NotificationsTable.type],
        title = this[NotificationsTable.title],
        body = this[NotificationsTable.body],
        referenceId = this[NotificationsTable.referenceId],
        referenceType = this[NotificationsTable.referenceType],
        read = this[NotificationsTable.read],
        createdAt = this[NotificationsTable.createdAt]
    )
}
