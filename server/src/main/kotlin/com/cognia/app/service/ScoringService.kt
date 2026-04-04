package com.cognia.app.service

import com.cognia.app.database.UserProfilesTable
import com.cognia.app.dto.quiz.AttemptResultResponse
import com.cognia.app.dto.quiz.QuestionResult
import com.cognia.app.model.Difficulty
import com.cognia.app.repository.QuizAttemptRepository
import com.cognia.app.repository.QuizRepository
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ScoringService(
    private val quizRepository: QuizRepository,
    private val attemptRepository: QuizAttemptRepository
) {

    fun scoreAttempt(quizId: String, userId: String, answers: List<Int>): AttemptResultResponse {
        val quiz = quizRepository.findById(quizId)
            ?: throw QuizNotFoundException(quizId)

        val questionsWithOptions = quizRepository.getQuestionsWithOptions(quizId)

        if (answers.size != questionsWithOptions.size) {
            throw IllegalArgumentException(
                "Expected ${questionsWithOptions.size} answers but received ${answers.size}"
            )
        }

        val results = questionsWithOptions.mapIndexed { index, qwo ->
            val selectedIndex = answers[index]
            val correctIndex = qwo.question.correctOptionIndex
            QuestionResult(
                questionId = qwo.question.id,
                selectedIndex = selectedIndex,
                correctIndex = correctIndex,
                isCorrect = selectedIndex == correctIndex
            )
        }

        val correctCount = results.count { it.isCorrect }
        val totalQuestions = questionsWithOptions.size

        // M5-004: Difficulty-based points
        val pointsPerCorrect = quiz.difficulty?.let { diffStr ->
            try {
                Difficulty.valueOf(diffStr).pointsPerCorrectAnswer
            } catch (_: IllegalArgumentException) {
                Difficulty.BASE_POINTS
            }
        } ?: Difficulty.BASE_POINTS

        val pointsAwarded = correctCount * pointsPerCorrect

        // Save attempt
        attemptRepository.saveAttempt(
            userId = userId,
            quizId = quizId,
            score = correctCount,
            totalQuestions = totalQuestions,
            pointsAwarded = pointsAwarded
        )

        // Update user profile: totalPoints and recalculate level
        updateUserProfile(userId, pointsAwarded)

        return AttemptResultResponse(
            score = correctCount,
            totalQuestions = totalQuestions,
            pointsAwarded = pointsAwarded,
            results = results
        )
    }

    private fun updateUserProfile(userId: String, pointsAwarded: Int) {
        transaction {
            // First get current points to calculate new level
            val currentPoints = UserProfilesTable
                .select(UserProfilesTable.totalPoints)
                .where { UserProfilesTable.userId eq userId }
                .singleOrNull()
                ?.get(UserProfilesTable.totalPoints) ?: 0

            val newTotalPoints = currentPoints + pointsAwarded
            val newLevel = newTotalPoints / 100 + 1

            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[totalPoints] = newTotalPoints
                it[level] = newLevel
            }
        }
    }
}
