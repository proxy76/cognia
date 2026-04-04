package com.cognia.app.repository

import com.cognia.app.database.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizRepositoryTest {

    private val repo = QuizRepository()

    private fun withTestDb(block: () -> Unit) {
        val tempFile = File.createTempFile("cognia-quiz-repo-test-", ".db")
        try {
            DatabaseFactory.init(tempFile.absolutePath)
            // Seed required parent rows
            transaction {
                UsersTable.insert {
                    it[id] = CREATOR_ID
                    it[email] = "creator@test.com"
                    it[displayName] = "Creator"
                    it[role] = "REGULAR_CREATOR"
                    it[authProvider] = "EMAIL"
                    it[createdAt] = "2026-01-01T00:00:00"
                    it[updatedAt] = "2026-01-01T00:00:00"
                }
                CategoriesTable.insert {
                    it[id] = CATEGORY_ID
                    it[name] = "Test Cat"
                    it[slug] = "test-cat"
                }
            }
            block()
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `create quiz with questions and options`() = withTestDb {
        val quiz = repo.createQuiz(CREATOR_ID, "My Quiz", "MULTIPLE_CHOICE", CATEGORY_ID, "EASY", null)
        assertNotNull(quiz.id)
        assertEquals("DRAFT", quiz.status)
        assertEquals("EASY", quiz.difficulty)

        val q1 = repo.addQuestion(quiz.id, "What is 1+1?", 1, 0)
        repo.addOption(q1.id, "1", 0)
        repo.addOption(q1.id, "2", 1)

        val q2 = repo.addQuestion(quiz.id, "What is 2+2?", 0, 1)
        repo.addOption(q2.id, "4", 0)
        repo.addOption(q2.id, "5", 1)

        val questionsWithOpts = repo.getQuestionsWithOptions(quiz.id)
        assertEquals(2, questionsWithOpts.size)
        assertEquals(2, questionsWithOpts[0].options.size)
        assertEquals(2, questionsWithOpts[1].options.size)
        assertEquals("What is 1+1?", questionsWithOpts[0].question.questionText)
        assertEquals(1, questionsWithOpts[0].question.correctOptionIndex)
    }

    @Test
    fun `findById returns quiz`() = withTestDb {
        val quiz = repo.createQuiz(CREATOR_ID, "Find Me", "TRUE_FALSE", CATEGORY_ID, null, null)
        val found = repo.findById(quiz.id)
        assertNotNull(found)
        assertEquals("Find Me", found.title)
    }

    @Test
    fun `findById returns null for missing`() = withTestDb {
        assertNull(repo.findById("nonexistent"))
    }

    @Test
    fun `findByCreatorId returns quizzes`() = withTestDb {
        repo.createQuiz(CREATOR_ID, "Quiz A", "MULTIPLE_CHOICE", CATEGORY_ID, null, null)
        repo.createQuiz(CREATOR_ID, "Quiz B", "MULTIPLE_CHOICE", CATEGORY_ID, null, null)
        val results = repo.findByCreatorId(CREATOR_ID)
        assertEquals(2, results.size)
    }

    @Test
    fun `updateStatus changes status`() = withTestDb {
        val quiz = repo.createQuiz(CREATOR_ID, "Status Test", "MULTIPLE_CHOICE", CATEGORY_ID, null, null)
        assertTrue(repo.updateStatus(quiz.id, "PENDING_REVIEW"))
        val updated = repo.findById(quiz.id)
        assertEquals("PENDING_REVIEW", updated?.status)
    }

    @Test
    fun `deleteQuiz cascades questions and options`() = withTestDb {
        val quiz = repo.createQuiz(CREATOR_ID, "Delete Me", "MULTIPLE_CHOICE", CATEGORY_ID, null, null)
        val q = repo.addQuestion(quiz.id, "Q?", 0, 0)
        repo.addOption(q.id, "A", 0)
        repo.addOption(q.id, "B", 1)

        assertTrue(repo.deleteQuiz(quiz.id))
        assertNull(repo.findById(quiz.id))
        assertEquals(0, repo.getQuestionsWithOptions(quiz.id).size)
    }

    @Test
    fun `getQuestionCount returns correct count`() = withTestDb {
        val quiz = repo.createQuiz(CREATOR_ID, "Count Test", "MULTIPLE_CHOICE", CATEGORY_ID, null, null)
        repo.addQuestion(quiz.id, "Q1", 0, 0)
        repo.addQuestion(quiz.id, "Q2", 0, 1)
        repo.addQuestion(quiz.id, "Q3", 0, 2)
        assertEquals(3, repo.getQuestionCount(quiz.id))
    }

    companion object {
        private const val CREATOR_ID = "creator-001"
        private const val CATEGORY_ID = "cat-001"
    }
}
