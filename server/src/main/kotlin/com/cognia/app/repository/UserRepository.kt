package com.cognia.app.repository

import com.cognia.app.database.UsersTable
import com.cognia.app.database.UserProfilesTable
import com.cognia.app.database.RefreshTokensTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class UserRepository {

    data class UserRow(
        val id: String,
        val email: String,
        val passwordHash: String?,
        val displayName: String,
        val role: String,
        val createdAt: String
    )

    fun findByEmail(email: String): UserRow? = transaction {
        UsersTable.selectAll().where { UsersTable.email eq email }
            .map { it.toUserRow() }
            .singleOrNull()
    }

    fun findById(id: String): UserRow? = transaction {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { it.toUserRow() }
            .singleOrNull()
    }

    fun createUser(email: String, passwordHash: String, displayName: String): UserRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        UsersTable.insert {
            it[UsersTable.id] = id
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.displayName] = displayName
            it[UsersTable.role] = "LEARNER"
            it[UsersTable.authProvider] = "EMAIL"
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }

        // Create user profile
        UserProfilesTable.insert {
            it[UserProfilesTable.userId] = id
            it[UserProfilesTable.level] = 1
            it[UserProfilesTable.totalPoints] = 0
        }

        UserRow(id, email, passwordHash, displayName, "LEARNER", now)
    }

    fun saveRefreshToken(userId: String, tokenHash: String, expiresAt: String): String = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        RefreshTokensTable.insert {
            it[RefreshTokensTable.id] = id
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.tokenHash] = tokenHash
            it[RefreshTokensTable.expiresAt] = expiresAt
            it[RefreshTokensTable.createdAt] = now
        }
        id
    }

    private fun ResultRow.toUserRow() = UserRow(
        id = this[UsersTable.id],
        email = this[UsersTable.email],
        passwordHash = this[UsersTable.passwordHash],
        displayName = this[UsersTable.displayName],
        role = this[UsersTable.role],
        createdAt = this[UsersTable.createdAt]
    )
}
