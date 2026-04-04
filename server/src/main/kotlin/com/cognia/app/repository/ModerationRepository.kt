package com.cognia.app.repository

import com.cognia.app.database.ModerationReviewsTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class ModerationRepository {

    data class ReviewRow(
        val id: String,
        val contentId: String,
        val contentType: String,
        val moderatorId: String?,
        val decision: String?,
        val reason: String?,
        val isPostPublication: Boolean,
        val createdAt: String,
        val decidedAt: String?
    )

    fun createReview(contentId: String, contentType: String, isPostPublication: Boolean = false): ReviewRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        ModerationReviewsTable.insert {
            it[ModerationReviewsTable.id] = id
            it[ModerationReviewsTable.contentId] = contentId
            it[ModerationReviewsTable.contentType] = contentType
            it[ModerationReviewsTable.isPostPublication] = if (isPostPublication) 1 else 0
            it[ModerationReviewsTable.createdAt] = now
        }

        ReviewRow(
            id = id,
            contentId = contentId,
            contentType = contentType,
            moderatorId = null,
            decision = null,
            reason = null,
            isPostPublication = isPostPublication,
            createdAt = now,
            decidedAt = null
        )
    }

    fun findById(id: String): ReviewRow? = transaction {
        ModerationReviewsTable.selectAll().where { ModerationReviewsTable.id eq id }
            .map { it.toReviewRow() }
            .singleOrNull()
    }

    fun getPendingReviews(): List<ReviewRow> = transaction {
        ModerationReviewsTable.selectAll()
            .where { ModerationReviewsTable.decision.isNull() }
            .orderBy(ModerationReviewsTable.createdAt, SortOrder.ASC)
            .map { it.toReviewRow() }
    }

    fun getReviewHistory(): List<ReviewRow> = transaction {
        ModerationReviewsTable.selectAll()
            .orderBy(ModerationReviewsTable.createdAt, SortOrder.DESC)
            .map { it.toReviewRow() }
    }

    fun updateDecision(id: String, moderatorId: String, decision: String, reason: String?): ReviewRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        val updated = ModerationReviewsTable.update({ ModerationReviewsTable.id eq id }) {
            it[ModerationReviewsTable.moderatorId] = moderatorId
            it[ModerationReviewsTable.decision] = decision
            it[ModerationReviewsTable.reason] = reason
            it[ModerationReviewsTable.decidedAt] = now
        }

        if (updated > 0) findById(id) else null
    }

    private fun ResultRow.toReviewRow() = ReviewRow(
        id = this[ModerationReviewsTable.id],
        contentId = this[ModerationReviewsTable.contentId],
        contentType = this[ModerationReviewsTable.contentType],
        moderatorId = this[ModerationReviewsTable.moderatorId],
        decision = this[ModerationReviewsTable.decision],
        reason = this[ModerationReviewsTable.reason],
        isPostPublication = this[ModerationReviewsTable.isPostPublication] != 0,
        createdAt = this[ModerationReviewsTable.createdAt],
        decidedAt = this[ModerationReviewsTable.decidedAt]
    )
}
