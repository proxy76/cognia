package com.cognia.app.service

import com.cognia.app.database.*
import com.cognia.app.repository.LicenseRepository
import com.cognia.app.repository.StrikeRepository
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class StrikeServiceTest {

    private fun withTestDb(block: () -> Unit) {
        val testDbFile = File.createTempFile("cognia-strike-test-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)
        block()
    }

    private fun seedUser(id: String, role: String = "REGULAR_CREATOR") {
        transaction {
            UsersTable.insert {
                it[UsersTable.id] = id
                it[email] = "$id@example.com"
                it[displayName] = "User $id"
                it[UsersTable.role] = role
                it[authProvider] = "EMAIL"
                it[createdAt] = "2024-01-01T00:00:00"
                it[updatedAt] = "2024-01-01T00:00:00"
            }
            UserProfilesTable.insert {
                it[userId] = id
                it[level] = 1
                it[totalPoints] = 0
            }
        }
    }

    @Test
    fun `issueStrike creates a strike with 7-day cooldown`() = withTestDb {
        seedUser("creator-1")
        seedUser("moderator-1", "MODERATOR")

        val strikeRepository = StrikeRepository()
        val licenseRepository = LicenseRepository()
        val service = StrikeService(strikeRepository, licenseRepository)

        val strike = service.issueStrike("creator-1", "moderator-1", "Violation")

        assertEquals("creator-1", strike.userId)
        assertEquals("moderator-1", strike.moderatorId)
        assertEquals("Violation", strike.reason)
        assertNotNull(strike.cooldownUntil)
        assertTrue(strike.cooldownUntil > strike.createdAt)
    }

    @Test
    fun `isInCooldown returns true after strike`() = withTestDb {
        seedUser("creator-2")
        seedUser("moderator-2", "MODERATOR")

        val strikeRepository = StrikeRepository()
        val licenseRepository = LicenseRepository()
        val service = StrikeService(strikeRepository, licenseRepository)

        assertFalse(service.isInCooldown("creator-2"))

        service.issueStrike("creator-2", "moderator-2", "Strike reason")

        assertTrue(service.isInCooldown("creator-2"))
    }

    @Test
    fun `issueStrike revokes license for LICENSED_CREATOR`() = withTestDb {
        seedUser("licensed-1", "LICENSED_CREATOR")
        seedUser("moderator-3", "MODERATOR")

        val strikeRepository = StrikeRepository()
        val licenseRepository = LicenseRepository()
        val service = StrikeService(strikeRepository, licenseRepository)

        service.issueStrike("licensed-1", "moderator-3", "License revoked")

        // Verify role was changed to REGULAR_CREATOR
        val user = transaction {
            UsersTable.selectAll().where { UsersTable.id eq "licensed-1" }.singleOrNull()
        }
        assertNotNull(user)
        assertEquals("REGULAR_CREATOR", user[UsersTable.role])
    }

    @Test
    fun `issueStrike revokes pending license requests`() = withTestDb {
        seedUser("creator-3", "REGULAR_CREATOR")
        seedUser("moderator-4", "MODERATOR")

        val strikeRepository = StrikeRepository()
        val licenseRepository = LicenseRepository()
        val service = StrikeService(strikeRepository, licenseRepository)

        // Create a pending license request
        licenseRepository.createRequest("creator-3")

        // Verify it's pending
        val pendingBefore = licenseRepository.getRequestsByCreator("creator-3")
        assertEquals(1, pendingBefore.size)
        assertEquals("PENDING", pendingBefore[0].status)

        // Issue strike
        service.issueStrike("creator-3", "moderator-4", "Bad content")

        // Verify pending request was revoked
        val requestsAfter = licenseRepository.getRequestsByCreator("creator-3")
        assertEquals(1, requestsAfter.size)
        assertEquals("REVOKED", requestsAfter[0].status)
    }

    @Test
    fun `issueStrike does not change role for non-licensed creator`() = withTestDb {
        seedUser("regular-1", "REGULAR_CREATOR")
        seedUser("moderator-5", "MODERATOR")

        val strikeRepository = StrikeRepository()
        val licenseRepository = LicenseRepository()
        val service = StrikeService(strikeRepository, licenseRepository)

        service.issueStrike("regular-1", "moderator-5", "Warning")

        val user = transaction {
            UsersTable.selectAll().where { UsersTable.id eq "regular-1" }.singleOrNull()
        }
        assertNotNull(user)
        assertEquals("REGULAR_CREATOR", user[UsersTable.role])
    }
}
