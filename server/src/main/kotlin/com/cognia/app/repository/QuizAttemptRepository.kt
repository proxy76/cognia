package com.cognia.app.repository

import com.cognia.app.database.QuizAttemptsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class QuizAttemptRepository {

    data class AttemptRow(
        val id: String,
        val userId: String,
        val quizId: String,
        val score: Int,
        val totalQuestions: Int,
        val pointsAwarded: Int,
        val completedAt: String
    )

    fun saveAttempt(
        userId: String,
        quizId: String,
        score: Int,
        totalQuestions: Int,
        pointsAwarded: Int
    ): AttemptRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        QuizAttemptsTable.insert {
            it[QuizAttemptsTable.id] = id
            it[QuizAttemptsTable.userId] = userId
            it[QuizAttemptsTable.quizId] = quizId
            it[QuizAttemptsTable.score] = score
            it[QuizAttemptsTable.totalQuestions] = totalQuestions
            it[QuizAttemptsTable.pointsAwarded] = pointsAwarded
            it[QuizAttemptsTable.completedAt] = now
        }

        AttemptRow(
            id = id,
            userId = userId,
            quizId = quizId,
            score = score,
            totalQuestions = totalQuestions,
            pointsAwarded = pointsAwarded,
            completedAt = now
        )
    }

    fun findByUserAndQuiz(userId: String, quizId: String): List<AttemptRow> = transaction {
        QuizAttemptsTable.selectAll()
            .where { (QuizAttemptsTable.userId eq userId) and (QuizAttemptsTable.quizId eq quizId) }
            .orderBy(QuizAttemptsTable.completedAt, SortOrder.DESC)
            .map { it.toAttemptRow() }
    }

    private fun ResultRow.toAttemptRow() = AttemptRow(
        id = this[QuizAttemptsTable.id],
        userId = this[QuizAttemptsTable.userId],
        quizId = this[QuizAttemptsTable.quizId],
        score = this[QuizAttemptsTable.score],
        totalQuestions = this[QuizAttemptsTable.totalQuestions],
        pointsAwarded = this[QuizAttemptsTable.pointsAwarded],
        completedAt = this[QuizAttemptsTable.completedAt]
    )
}
