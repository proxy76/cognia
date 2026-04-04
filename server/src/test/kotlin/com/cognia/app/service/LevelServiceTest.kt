package com.cognia.app.service

import com.cognia.app.database.DatabaseFactory
import com.cognia.app.database.UserProfilesTable
import com.cognia.app.database.UsersTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LevelServiceTest {

    private val levelService = LevelService()

    @Test
    fun `level 1 at 0 points`() {
        assertEquals(1, levelService.calculateLevel(0))
    }

    @Test
    fun `level 1 at 99 points`() {
        assertEquals(1, levelService.calculateLevel(99))
    }

    @Test
    fun `level 2 at 100 points`() {
        assertEquals(2, levelService.calculateLevel(100))
    }

    @Test
    fun `level 3 at 200 points`() {
        assertEquals(3, levelService.calculateLevel(200))
    }

    @Test
    fun `level 11 at 1000 points`() {
        assertEquals(11, levelService.calculateLevel(1000))
    }

    @Test
    fun `level-up detected correctly`() {
        val testDbFile = File.createTempFile("cognia-level-test-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)

        val userId = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        transaction {
            UsersTable.insert {
                it[id] = userId
                it[email] = "level-test@example.com"
                it[displayName] = "Level Tester"
                it[role] = "LEARNER"
                it[authProvider] = "EMAIL"
                it[createdAt] = now
                it[updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[level] = 1
                it[totalPoints] = 50
            }
        }

        val result = levelService.updateUserLevel(userId, 150)
        assertTrue(result.leveledUp)
        assertEquals(1, result.oldLevel)
        assertEquals(2, result.newLevel)

        // Verify DB was updated
        val dbLevel = transaction {
            UserProfilesTable.selectAll()
                .where { UserProfilesTable.userId eq userId }
                .single()[UserProfilesTable.level]
        }
        assertEquals(2, dbLevel)
    }

    @Test
    fun `no level-up when points stay in same level`() {
        val testDbFile = File.createTempFile("cognia-level-test2-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)

        val userId = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        transaction {
            UsersTable.insert {
                it[id] = userId
                it[email] = "level-test2@example.com"
                it[displayName] = "Level Tester 2"
                it[role] = "LEARNER"
                it[authProvider] = "EMAIL"
                it[createdAt] = now
                it[updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[level] = 1
                it[totalPoints] = 50
            }
        }

        val result = levelService.updateUserLevel(userId, 90)
        assertFalse(result.leveledUp)
        assertEquals(1, result.oldLevel)
        assertEquals(1, result.newLevel)
    }

    @Test
    fun `no level-down when level already higher`() {
        val testDbFile = File.createTempFile("cognia-level-test3-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)

        val userId = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        transaction {
            UsersTable.insert {
                it[id] = userId
                it[email] = "level-test3@example.com"
                it[displayName] = "Level Tester 3"
                it[role] = "LEARNER"
                it[authProvider] = "EMAIL"
                it[createdAt] = now
                it[updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[level] = 5
                it[totalPoints] = 400
            }
        }

        // Points that would compute to level 3, but stored level is 5 — no level-down
        val result = levelService.updateUserLevel(userId, 250)
        assertFalse(result.leveledUp)
        assertEquals(5, result.oldLevel)
        assertEquals(3, result.newLevel)

        // Verify DB was NOT updated — level stays at 5
        val dbLevel = transaction {
            UserProfilesTable.selectAll()
                .where { UserProfilesTable.userId eq userId }
                .single()[UserProfilesTable.level]
        }
        assertEquals(5, dbLevel)
    }
}
