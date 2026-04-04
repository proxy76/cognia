package com.cognia.app.service

import com.cognia.app.dto.chat.*
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.repository.ChatRepository

class ChatService(
    private val chatRepository: ChatRepository,
    private val friendshipService: FriendshipService
) {

    fun createConversation(currentUserId: String, participantId: String): ConversationResponse {
        require(currentUserId != participantId) { "Cannot create a conversation with yourself" }

        // Only friends can chat
        if (!friendshipService.areFriends(currentUserId, participantId)) {
            throw NotFriendsException()
        }

        // Check if conversation already exists
        val existing = chatRepository.findConversation(currentUserId, participantId)
        if (existing != null) {
            return ConversationResponse(
                id = existing.id,
                participant = UserSummary(id = participantId, displayName = "", avatarUrl = null),
                lastMessage = null
            )
        }

        val conv = chatRepository.createConversation(currentUserId, participantId)
        return ConversationResponse(
            id = conv.id,
            participant = UserSummary(id = participantId, displayName = "", avatarUrl = null),
            lastMessage = null
        )
    }

    fun getConversations(userId: String): ConversationListResponse {
        val conversations = chatRepository.getConversations(userId).map { conv ->
            ConversationResponse(
                id = conv.id,
                participant = UserSummary(
                    id = conv.participantId,
                    displayName = conv.participantDisplayName,
                    avatarUrl = conv.participantAvatarUrl
                ),
                lastMessage = if (conv.lastMessageAt != null) {
                    LastMessageSummary(
                        text = conv.lastMessageText,
                        createdAt = conv.lastMessageAt
                    )
                } else null
            )
        }
        return ConversationListResponse(conversations = conversations)
    }

    fun sendMessage(
        conversationId: String,
        senderId: String,
        messageType: String,
        textContent: String?,
        sharedContentId: String?,
        sharedContentType: String?
    ): ChatMessageResponse {
        // Verify sender is a participant
        if (!chatRepository.isParticipant(conversationId, senderId)) {
            throw ConversationAccessDeniedException()
        }

        val message = chatRepository.sendMessage(
            conversationId = conversationId,
            senderId = senderId,
            messageType = messageType,
            textContent = textContent,
            sharedContentId = sharedContentId,
            sharedContentType = sharedContentType
        )

        return message.toResponse()
    }

    fun getMessages(conversationId: String, userId: String, limit: Int = 50, cursor: String? = null): MessageListResponse {
        if (!chatRepository.isParticipant(conversationId, userId)) {
            throw ConversationAccessDeniedException()
        }

        val clampedLimit = limit.coerceIn(1, 100)
        val messages = chatRepository.getMessages(conversationId, clampedLimit, cursor)

        val nextCursor = if (messages.size == clampedLimit) messages.first().createdAt else null

        return MessageListResponse(
            messages = messages.map { it.toResponse() },
            nextCursor = nextCursor
        )
    }

    fun getConversationParticipants(conversationId: String): Pair<String, String>? {
        val conv = chatRepository.getConversationById(conversationId) ?: return null
        return conv.participantA to conv.participantB
    }

    private fun ChatRepository.MessageRow.toResponse() = ChatMessageResponse(
        id = id,
        senderId = senderId,
        messageType = messageType,
        textContent = textContent,
        sharedContentId = sharedContentId,
        sharedContentType = sharedContentType,
        createdAt = createdAt
    )
}

class NotFriendsException : RuntimeException("Only friends can chat with each other")
class ConversationAccessDeniedException : RuntimeException("You are not a participant of this conversation")
