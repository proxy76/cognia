package com.cognia.app.service

import com.cognia.app.database.CategoriesTable
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.database.QuizAttemptsTable
import com.cognia.app.database.QuizzesTable
import com.cognia.app.database.UserBadgesTable
import com.cognia.app.database.UserProfilesTable
import com.cognia.app.database.UsersTable
import com.cognia.app.repository.BadgeRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BadgeServiceTest {

    private fun setupTestDb(): Pair<BadgeService, BadgeRepository> {
        val testDbFile = File.createTempFile("cognia-badge-test-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)

        val badgeRepository = BadgeRepository()
        val badgeService = BadgeService(badgeRepository)
        badgeService.seedBadges()
        return Pair(badgeService, badgeRepository)
    }

    private fun createTestUser(email: String = "badge-test@example.com", displayName: String = "Badge Tester"): String {
        val userId = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        transaction {
            UsersTable.insert {
                it[id] = userId
                it[UsersTable.email] = email
                it[UsersTable.displayName] = displayName
                it[role] = "LEARNER"
                it[authProvider] = "EMAIL"
                it[createdAt] = now
                it[updatedAt] = now
            }
            UserProfilesTable.insert {
                it[UserProfilesTable.userId] = userId
                it[level] = 1
                it[totalPoints] = 0
            }
        }
        return userId
    }

    private fun ensureCategoryExists(categoryId: String = "test-cat") {
        transaction {
            val exists = CategoriesTable.selectAll()
                .where { CategoriesTable.id eq categoryId }
                .count() > 0
            if (!exists) {
                CategoriesTable.insert {
                    it[id] = categoryId
                    it[name] = "Test Category"
                    it[slug] = "test-category"
                }
            }
        }
    }

    private fun ensureQuizExists(quizId: String, creatorId: String) {
        ensureCategoryExists()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        transaction {
            val exists = QuizzesTable.selectAll()
                .where { QuizzesTable.id eq quizId }
                .count() > 0
            if (!exists) {
                QuizzesTable.insert {
                    it[id] = quizId
                    it[QuizzesTable.creatorId] = creatorId
                    it[title] = "Test Quiz"
                    it[quizType] = "MULTIPLE_CHOICE"
                    it[categoryId] = "test-cat"
                    it[status] = "PUBLISHED"
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
        }
    }

    private fun insertQuizAttempt(userId: String, quizId: String, score: Int, total: Int) {
        ensureQuizExists(quizId, userId)
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        transaction {
            QuizAttemptsTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[QuizAttemptsTable.userId] = userId
                it[QuizAttemptsTable.quizId] = quizId
                it[QuizAttemptsTable.score] = score
                it[totalQuestions] = total
                it[pointsAwarded] = score * 10
                it[completedAt] = now
            }
        }
    }

    @Test
    fun `first quiz badge awarded after one quiz completion`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        // Insert a quiz attempt so the count is 1
        insertQuizAttempt(userId, "quiz-1", 8, 10)

        badgeService.checkAndAwardBadges(userId, BadgeEvent.QuizCompleted(userId, isPerfectScore = false))

        assertTrue(badgeRepository.hasUserBadge(userId, "FIRST_QUIZ"))
        assertFalse(badgeRepository.hasUserBadge(userId, "QUIZ_10"))
    }

    @Test
    fun `duplicate badge not awarded`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        insertQuizAttempt(userId, "quiz-1", 8, 10)

        badgeService.checkAndAwardBadges(userId, BadgeEvent.QuizCompleted(userId, isPerfectScore = false))
        assertTrue(badgeRepository.hasUserBadge(userId, "FIRST_QUIZ"))

        // Award again — should not throw or create duplicate
        val awarded = badgeRepository.awardBadge(userId, "FIRST_QUIZ")
        assertFalse(awarded)

        // Count should still be 1
        val count = transaction {
            UserBadgesTable.selectAll()
                .where { (UserBadgesTable.userId eq userId) and (UserBadgesTable.badgeId eq "FIRST_QUIZ") }
                .count()
        }
        assertEquals(1L, count)
    }

    @Test
    fun `perfect score badge awarded`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        insertQuizAttempt(userId, "quiz-1", 10, 10)

        badgeService.checkAndAwardBadges(userId, BadgeEvent.QuizCompleted(userId, isPerfectScore = true))

        assertTrue(badgeRepository.hasUserBadge(userId, "FIRST_QUIZ"))
        assertTrue(badgeRepository.hasUserBadge(userId, "PERFECT_SCORE"))
    }

    @Test
    fun `level 5 badge awarded at correct threshold`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        badgeService.checkAndAwardBadges(userId, BadgeEvent.LevelUp(userId, newLevel = 5))

        assertTrue(badgeRepository.hasUserBadge(userId, "LEVEL_5"))
        assertFalse(badgeRepository.hasUserBadge(userId, "LEVEL_10"))
    }

    @Test
    fun `level 10 badge awarded at correct threshold`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        badgeService.checkAndAwardBadges(userId, BadgeEvent.LevelUp(userId, newLevel = 10))

        assertTrue(badgeRepository.hasUserBadge(userId, "LEVEL_5"))
        assertTrue(badgeRepository.hasUserBadge(userId, "LEVEL_10"))
    }

    @Test
    fun `level 3 does not award level 5 badge`() {
        val (badgeService, badgeRepository) = setupTestDb()
        val userId = createTestUser()

        badgeService.checkAndAwardBadges(userId, BadgeEvent.LevelUp(userId, newLevel = 3))

        assertFalse(badgeRepository.hasUserBadge(userId, "LEVEL_5"))
    }

    @Test
    fun `getUserBadges returns awarded badges`() {
        val (badgeService, _) = setupTestDb()
        val userId = createTestUser()

        insertQuizAttempt(userId, "quiz-1", 10, 10)
        badgeService.checkAndAwardBadges(userId, BadgeEvent.QuizCompleted(userId, isPerfectScore = true))

        val userBadges = badgeService.getUserBadges(userId)
        assertTrue(userBadges.size >= 2)

        val badgeIds = userBadges.map { it.badge.id }
        assertTrue("FIRST_QUIZ" in badgeIds)
        assertTrue("PERFECT_SCORE" in badgeIds)
    }

    @Test
    fun `seedBadges creates all badge definitions`() {
        val (badgeService, badgeRepository) = setupTestDb()

        val allBadges = badgeRepository.findAll()
        assertEquals(7, allBadges.size)

        val ids = allBadges.map { it.id }.toSet()
        assertTrue("FIRST_QUIZ" in ids)
        assertTrue("QUIZ_10" in ids)
        assertTrue("QUIZ_50" in ids)
        assertTrue("FIRST_VIDEO" in ids)
        assertTrue("LEVEL_5" in ids)
        assertTrue("LEVEL_10" in ids)
        assertTrue("PERFECT_SCORE" in ids)
    }
}
