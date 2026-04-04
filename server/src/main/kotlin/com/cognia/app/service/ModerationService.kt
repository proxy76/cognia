package com.cognia.app.service

import com.cognia.app.database.QuizzesTable
import com.cognia.app.database.UsersTable
import com.cognia.app.database.VideosTable
import com.cognia.app.dto.moderation.ModerationQueueItem
import com.cognia.app.dto.moderation.ModerationReviewResponse
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.repository.ModerationRepository
import com.cognia.app.repository.VideoRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class ModerationService(
    private val moderationRepository: ModerationRepository,
    private val videoRepository: VideoRepository
) {

    fun getPendingQueue(): List<ModerationQueueItem> {
        val reviews = moderationRepository.getPendingReviews()
        return reviews.mapNotNull { review ->
            val contentInfo = lookupContentInfo(review.contentId, review.contentType) ?: return@mapNotNull null
            ModerationQueueItem(
                reviewId = review.id,
                contentType = review.contentType,
                contentId = review.contentId,
                title = contentInfo.first,
                creator = contentInfo.second,
                isPostPublication = review.isPostPublication,
                submittedAt = review.createdAt
            )
        }
    }

    fun getReviewHistory(): List<ModerationReviewResponse> {
        return moderationRepository.getReviewHistory().map { it.toResponse() }
    }

    fun getReviewById(id: String): ModerationReviewResponse? {
        return moderationRepository.findById(id)?.toResponse()
    }

    fun createReview(contentId: String, contentType: String, isPostPublication: Boolean = false): ModerationReviewResponse {
        val review = moderationRepository.createReview(contentId, contentType, isPostPublication)
        return review.toResponse()
    }

    fun decide(reviewId: String, moderatorId: String, decision: String, reason: String?): ModerationReviewResponse {
        val review = moderationRepository.findById(reviewId)
            ?: throw ModerationNotFoundException("Review not found: $reviewId")

        if (review.decision != null) {
            throw ModerationAlreadyDecidedException("Review $reviewId has already been decided")
        }

        if (decision !in setOf("APPROVED", "REJECTED")) {
            throw IllegalArgumentException("Decision must be APPROVED or REJECTED")
        }

        // Transition content status
        when (decision) {
            "APPROVED" -> {
                when (review.contentType) {
                    "VIDEO" -> videoRepository.updateStatus(review.contentId, "PUBLISHED")
                    "QUIZ" -> {
                        transaction {
                            val now = kotlinx.datetime.Clock.System.now()
                                .toLocalDateTime(kotlinx.datetime.TimeZone.UTC).toString()
                            QuizzesTable.update({ QuizzesTable.id eq review.contentId }) {
                                it[status] = "PUBLISHED"
                                it[updatedAt] = now
                            }
                        }
                    }
                }
            }
            "REJECTED" -> {
                when (review.contentType) {
                    "VIDEO" -> videoRepository.updateStatus(review.contentId, "REJECTED")
                    "QUIZ" -> {
                        transaction {
                            val now = kotlinx.datetime.Clock.System.now()
                                .toLocalDateTime(kotlinx.datetime.TimeZone.UTC).toString()
                            QuizzesTable.update({ QuizzesTable.id eq review.contentId }) {
                                it[status] = "REJECTED"
                                it[updatedAt] = now
                            }
                        }
                    }
                }
            }
        }

        val updated = moderationRepository.updateDecision(reviewId, moderatorId, decision, reason)
            ?: throw ModerationNotFoundException("Review not found: $reviewId")

        return updated.toResponse()
    }

    /**
     * Returns the content creator's userId for the given content, used by strike logic.
     */
    fun getContentCreatorId(contentId: String, contentType: String): String? {
        return when (contentType) {
            "VIDEO" -> videoRepository.findById(contentId)?.creatorId
            "QUIZ" -> transaction {
                QuizzesTable.selectAll().where { QuizzesTable.id eq contentId }
                    .singleOrNull()?.get(QuizzesTable.creatorId)
            }
            else -> null
        }
    }

    private fun lookupContentInfo(contentId: String, contentType: String): Pair<String, UserSummary>? {
        return when (contentType) {
            "VIDEO" -> {
                transaction {
                    val video = VideosTable.selectAll().where { VideosTable.id eq contentId }.singleOrNull()
                        ?: return@transaction null
                    val creatorId = video[VideosTable.creatorId]
                    val user = UsersTable.selectAll().where { UsersTable.id eq creatorId }.singleOrNull()
                        ?: return@transaction null
                    Pair(
                        video[VideosTable.title],
                        UserSummary(
                            id = user[UsersTable.id],
                            displayName = user[UsersTable.displayName],
                            avatarUrl = user[UsersTable.avatarUrl]
                        )
                    )
                }
            }
            "QUIZ" -> {
                transaction {
                    val quiz = QuizzesTable.selectAll().where { QuizzesTable.id eq contentId }.singleOrNull()
                        ?: return@transaction null
                    val creatorId = quiz[QuizzesTable.creatorId]
                    val user = UsersTable.selectAll().where { UsersTable.id eq creatorId }.singleOrNull()
                        ?: return@transaction null
                    Pair(
                        quiz[QuizzesTable.title],
                        UserSummary(
                            id = user[UsersTable.id],
                            displayName = user[UsersTable.displayName],
                            avatarUrl = user[UsersTable.avatarUrl]
                        )
                    )
                }
            }
            else -> null
        }
    }

    private fun ModerationRepository.ReviewRow.toResponse() = ModerationReviewResponse(
        id = id,
        contentId = contentId,
        contentType = contentType,
        moderatorId = moderatorId,
        decision = decision,
        reason = reason,
        isPostPublication = isPostPublication,
        createdAt = createdAt,
        decidedAt = decidedAt
    )
}

class ModerationNotFoundException(message: String) : RuntimeException(message)
class ModerationAlreadyDecidedException(message: String) : RuntimeException(message)
