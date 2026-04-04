package com.cognia.app.repository

import com.cognia.app.database.QuizOptionsTable
import com.cognia.app.database.QuizQuestionsTable
import com.cognia.app.database.QuizzesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class QuizRepository {

    data class QuizRow(
        val id: String,
        val creatorId: String,
        val title: String,
        val quizType: String,
        val categoryId: String,
        val difficulty: String?,
        val videoId: String?,
        val status: String,
        val createdAt: String
    )

    data class QuestionRow(
        val id: String,
        val quizId: String,
        val questionText: String,
        val correctOptionIndex: Int,
        val orderIndex: Int
    )

    data class OptionRow(
        val id: String,
        val questionId: String,
        val text: String,
        val orderIndex: Int
    )

    data class QuestionWithOptions(
        val question: QuestionRow,
        val options: List<OptionRow>
    )

    fun createQuiz(
        creatorId: String,
        title: String,
        quizType: String,
        categoryId: String,
        difficulty: String?,
        videoId: String?
    ): QuizRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        QuizzesTable.insert {
            it[QuizzesTable.id] = id
            it[QuizzesTable.creatorId] = creatorId
            it[QuizzesTable.title] = title
            it[QuizzesTable.quizType] = quizType
            it[QuizzesTable.categoryId] = categoryId
            it[QuizzesTable.difficulty] = difficulty
            it[QuizzesTable.videoId] = videoId
            it[QuizzesTable.status] = "DRAFT"
            it[QuizzesTable.createdAt] = now
            it[QuizzesTable.updatedAt] = now
        }

        QuizRow(
            id = id,
            creatorId = creatorId,
            title = title,
            quizType = quizType,
            categoryId = categoryId,
            difficulty = difficulty,
            videoId = videoId,
            status = "DRAFT",
            createdAt = now
        )
    }

    fun findById(id: String): QuizRow? = transaction {
        QuizzesTable.selectAll().where { QuizzesTable.id eq id }
            .map { it.toQuizRow() }
            .singleOrNull()
    }

    fun findByCreatorId(creatorId: String): List<QuizRow> = transaction {
        QuizzesTable.selectAll()
            .where { QuizzesTable.creatorId eq creatorId }
            .orderBy(QuizzesTable.createdAt, SortOrder.DESC)
            .map { it.toQuizRow() }
    }

    fun findByVideoId(videoId: String): List<QuizRow> = transaction {
        QuizzesTable.selectAll()
            .where { QuizzesTable.videoId eq videoId }
            .map { it.toQuizRow() }
    }

    fun updateStatus(id: String, status: String): Boolean = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        QuizzesTable.update({ QuizzesTable.id eq id }) {
            it[QuizzesTable.status] = status
            it[QuizzesTable.updatedAt] = now
        } > 0
    }

    fun deleteQuiz(id: String): Boolean = transaction {
        // Cascade: delete options -> questions -> quiz
        val questionIds = QuizQuestionsTable.selectAll()
            .where { QuizQuestionsTable.quizId eq id }
            .map { it[QuizQuestionsTable.id] }

        for (qId in questionIds) {
            QuizOptionsTable.deleteWhere { QuizOptionsTable.questionId eq qId }
        }
        QuizQuestionsTable.deleteWhere { QuizQuestionsTable.quizId eq id }
        QuizzesTable.deleteWhere { QuizzesTable.id eq id } > 0
    }

    fun addQuestion(
        quizId: String,
        questionText: String,
        correctOptionIndex: Int,
        orderIndex: Int
    ): QuestionRow = transaction {
        val id = UUID.randomUUID().toString()

        QuizQuestionsTable.insert {
            it[QuizQuestionsTable.id] = id
            it[QuizQuestionsTable.quizId] = quizId
            it[QuizQuestionsTable.questionText] = questionText
            it[QuizQuestionsTable.correctOptionIndex] = correctOptionIndex
            it[QuizQuestionsTable.orderIndex] = orderIndex
        }

        QuestionRow(
            id = id,
            quizId = quizId,
            questionText = questionText,
            correctOptionIndex = correctOptionIndex,
            orderIndex = orderIndex
        )
    }

    fun addOption(
        questionId: String,
        text: String,
        orderIndex: Int
    ): OptionRow = transaction {
        val id = UUID.randomUUID().toString()

        QuizOptionsTable.insert {
            it[QuizOptionsTable.id] = id
            it[QuizOptionsTable.questionId] = questionId
            it[QuizOptionsTable.text] = text
            it[QuizOptionsTable.orderIndex] = orderIndex
        }

        OptionRow(
            id = id,
            questionId = questionId,
            text = text,
            orderIndex = orderIndex
        )
    }

    fun getQuestionsWithOptions(quizId: String): List<QuestionWithOptions> = transaction {
        val questions = QuizQuestionsTable.selectAll()
            .where { QuizQuestionsTable.quizId eq quizId }
            .orderBy(QuizQuestionsTable.orderIndex)
            .map { it.toQuestionRow() }

        questions.map { question ->
            val options = QuizOptionsTable.selectAll()
                .where { QuizOptionsTable.questionId eq question.id }
                .orderBy(QuizOptionsTable.orderIndex)
                .map { it.toOptionRow() }
            QuestionWithOptions(question, options)
        }
    }

    fun getQuestionCount(quizId: String): Int = transaction {
        QuizQuestionsTable.selectAll()
            .where { QuizQuestionsTable.quizId eq quizId }
            .count()
            .toInt()
    }

    private fun ResultRow.toQuizRow() = QuizRow(
        id = this[QuizzesTable.id],
        creatorId = this[QuizzesTable.creatorId],
        title = this[QuizzesTable.title],
        quizType = this[QuizzesTable.quizType],
        categoryId = this[QuizzesTable.categoryId],
        difficulty = this[QuizzesTable.difficulty],
        videoId = this[QuizzesTable.videoId],
        status = this[QuizzesTable.status],
        createdAt = this[QuizzesTable.createdAt]
    )

    private fun ResultRow.toQuestionRow() = QuestionRow(
        id = this[QuizQuestionsTable.id],
        quizId = this[QuizQuestionsTable.quizId],
        questionText = this[QuizQuestionsTable.questionText],
        correctOptionIndex = this[QuizQuestionsTable.correctOptionIndex],
        orderIndex = this[QuizQuestionsTable.orderIndex]
    )

    private fun ResultRow.toOptionRow() = OptionRow(
        id = this[QuizOptionsTable.id],
        questionId = this[QuizOptionsTable.questionId],
        text = this[QuizOptionsTable.text],
        orderIndex = this[QuizOptionsTable.orderIndex]
    )
}
