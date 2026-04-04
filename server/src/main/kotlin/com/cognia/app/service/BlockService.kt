package com.cognia.app.service

import com.cognia.app.database.UsersTable
import com.cognia.app.dto.moderation.BlockedUserResponse
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.repository.BlockRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class BlockService(
    private val blockRepository: BlockRepository
) {

    fun blockUser(blockerId: String, blockedId: String): BlockedUserResponse {
        require(blockerId != blockedId) { "Cannot block yourself" }

        // Verify the target user exists
        val targetUser = transaction {
            UsersTable.selectAll().where { UsersTable.id eq blockedId }.singleOrNull()
        } ?: throw BlockedUserNotFoundException("User not found: $blockedId")

        if (blockRepository.isBlocked(blockerId, blockedId)) {
            throw AlreadyBlockedException("User is already blocked")
        }

        val block = blockRepository.block(blockerId, blockedId)
        return BlockedUserResponse(
            id = block.id,
            blockedUser = UserSummary(
                id = blockedId,
                displayName = targetUser[UsersTable.displayName],
                avatarUrl = targetUser[UsersTable.avatarUrl]
            ),
            createdAt = block.createdAt
        )
    }

    fun unblockUser(blockerId: String, blockedId: String) {
        if (!blockRepository.unblock(blockerId, blockedId)) {
            throw BlockNotFoundException("Block not found")
        }
    }

    fun getBlockedUsers(userId: String): List<BlockedUserResponse> {
        return blockRepository.getBlockedUsers(userId).map { row ->
            BlockedUserResponse(
                id = row.id,
                blockedUser = UserSummary(
                    id = row.blockedUserId,
                    displayName = row.blockedDisplayName,
                    avatarUrl = row.blockedAvatarUrl
                ),
                createdAt = row.createdAt
            )
        }
    }

    fun isBlocked(blockerId: String, blockedId: String): Boolean {
        return blockRepository.isBlocked(blockerId, blockedId)
    }

    fun getBlockedUserIds(userId: String): Set<String> {
        return blockRepository.getBlockedUserIds(userId)
    }
}

class BlockedUserNotFoundException(message: String) : RuntimeException(message)
class AlreadyBlockedException(message: String) : RuntimeException(message)
class BlockNotFoundException(message: String) : RuntimeException(message)
