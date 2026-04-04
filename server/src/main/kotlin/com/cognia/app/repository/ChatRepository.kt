package com.cognia.app.repository

import com.cognia.app.database.ChatConversationsTable
import com.cognia.app.database.ChatMessagesTable
import com.cognia.app.database.UsersTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.UUID

class ChatRepository {

    data class ConversationRow(
        val id: String,
        val participantA: String,
        val participantB: String,
        val createdAt: String,
        val lastMessageAt: String?
    )

    data class ConversationWithParticipantRow(
        val id: String,
        val participantId: String,
        val participantDisplayName: String,
        val participantAvatarUrl: String?,
        val lastMessageAt: String?,
        val lastMessageText: String?
    )

    data class MessageRow(
        val id: String,
        val conversationId: String,
        val senderId: String,
        val messageType: String,
        val textContent: String?,
        val sharedContentId: String?,
        val sharedContentType: String?,
        val createdAt: String
    )

    fun createConversation(participantA: String, participantB: String): ConversationRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        ChatConversationsTable.insert {
            it[ChatConversationsTable.id] = id
            it[ChatConversationsTable.participantA] = participantA
            it[ChatConversationsTable.participantB] = participantB
            it[ChatConversationsTable.createdAt] = now
        }
        ConversationRow(id, participantA, participantB, now, null)
    }

    fun findConversation(participantA: String, participantB: String): ConversationRow? = transaction {
        ChatConversationsTable.selectAll().where {
            ((ChatConversationsTable.participantA eq participantA) and (ChatConversationsTable.participantB eq participantB)) or
            ((ChatConversationsTable.participantA eq participantB) and (ChatConversationsTable.participantB eq participantA))
        }.map { it.toConversationRow() }.singleOrNull()
    }

    fun getConversationById(id: String): ConversationRow? = transaction {
        ChatConversationsTable.selectAll().where { ChatConversationsTable.id eq id }
            .map { it.toConversationRow() }
            .singleOrNull()
    }

    fun getConversations(userId: String): List<ConversationWithParticipantRow> = transaction {
        // Get all conversations where user is a participant
        val conversations = ChatConversationsTable.selectAll().where {
            (ChatConversationsTable.participantA eq userId) or (ChatConversationsTable.participantB eq userId)
        }.orderBy(
            ChatConversationsTable.lastMessageAt to SortOrder.DESC_NULLS_LAST,
            ChatConversationsTable.createdAt to SortOrder.DESC
        ).map { it.toConversationRow() }

        conversations.map { conv ->
            val otherUserId = if (conv.participantA == userId) conv.participantB else conv.participantA
            val otherUser = UsersTable.selectAll().where { UsersTable.id eq otherUserId }.singleOrNull()

            // Get last message
            val lastMessage = ChatMessagesTable.selectAll()
                .where { ChatMessagesTable.conversationId eq conv.id }
                .orderBy(ChatMessagesTable.createdAt, SortOrder.DESC)
                .limit(1)
                .singleOrNull()

            ConversationWithParticipantRow(
                id = conv.id,
                participantId = otherUserId,
                participantDisplayName = otherUser?.get(UsersTable.displayName) ?: "Unknown",
                participantAvatarUrl = otherUser?.get(UsersTable.avatarUrl),
                lastMessageAt = conv.lastMessageAt,
                lastMessageText = lastMessage?.get(ChatMessagesTable.textContent)
            )
        }
    }

    fun sendMessage(
        conversationId: String,
        senderId: String,
        messageType: String,
        textContent: String?,
        sharedContentId: String?,
        sharedContentType: String?
    ): MessageRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        ChatMessagesTable.insert {
            it[ChatMessagesTable.id] = id
            it[ChatMessagesTable.conversationId] = conversationId
            it[ChatMessagesTable.senderId] = senderId
            it[ChatMessagesTable.messageType] = messageType
            it[ChatMessagesTable.textContent] = textContent
            it[ChatMessagesTable.sharedContentId] = sharedContentId
            it[ChatMessagesTable.sharedContentType] = sharedContentType
            it[ChatMessagesTable.createdAt] = now
        }

        // Update last_message_at on the conversation
        ChatConversationsTable.update({ ChatConversationsTable.id eq conversationId }) {
            it[lastMessageAt] = now
        }

        MessageRow(id, conversationId, senderId, messageType, textContent, sharedContentId, sharedContentType, now)
    }

    fun getMessages(conversationId: String, limit: Int = 50, beforeCursor: String? = null): List<MessageRow> = transaction {
        val query = ChatMessagesTable.selectAll()
            .where { ChatMessagesTable.conversationId eq conversationId }

        if (beforeCursor != null) {
            query.andWhere { ChatMessagesTable.createdAt less beforeCursor }
        }

        query
            .orderBy(ChatMessagesTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .map { it.toMessageRow() }
            .reversed() // Return in chronological order
    }

    fun isParticipant(conversationId: String, userId: String): Boolean = transaction {
        ChatConversationsTable.selectAll().where {
            (ChatConversationsTable.id eq conversationId) and
            ((ChatConversationsTable.participantA eq userId) or (ChatConversationsTable.participantB eq userId))
        }.count() > 0
    }

    private fun ResultRow.toConversationRow() = ConversationRow(
        id = this[ChatConversationsTable.id],
        participantA = this[ChatConversationsTable.participantA],
        participantB = this[ChatConversationsTable.participantB],
        createdAt = this[ChatConversationsTable.createdAt],
        lastMessageAt = this[ChatConversationsTable.lastMessageAt]
    )

    private fun ResultRow.toMessageRow() = MessageRow(
        id = this[ChatMessagesTable.id],
        conversationId = this[ChatMessagesTable.conversationId],
        senderId = this[ChatMessagesTable.senderId],
        messageType = this[ChatMessagesTable.messageType],
        textContent = this[ChatMessagesTable.textContent],
        sharedContentId = this[ChatMessagesTable.sharedContentId],
        sharedContentType = this[ChatMessagesTable.sharedContentType],
        createdAt = this[ChatMessagesTable.createdAt]
    )
}
