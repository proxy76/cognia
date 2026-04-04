package com.cognia.app.service

import com.cognia.app.database.QuizAttemptsTable
import com.cognia.app.dto.gamification.BadgeResponse
import com.cognia.app.dto.gamification.UserBadgeResponse
import com.cognia.app.repository.BadgeRepository
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

sealed class BadgeEvent {
    data class QuizCompleted(val userId: String, val isPerfectScore: Boolean) : BadgeEvent()
    data class LevelUp(val userId: String, val newLevel: Int) : BadgeEvent()
    data class VideoWatched(val userId: String) : BadgeEvent()
}

class BadgeService(
    private val badgeRepository: BadgeRepository
) {

    fun checkAndAwardBadges(userId: String, event: BadgeEvent) {
        when (event) {
            is BadgeEvent.QuizCompleted -> handleQuizCompleted(userId, event)
            is BadgeEvent.LevelUp -> handleLevelUp(userId, event)
            is BadgeEvent.VideoWatched -> handleVideoWatched(userId)
        }
    }

    fun getUserBadges(userId: String): List<UserBadgeResponse> {
        return badgeRepository.getUserBadges(userId).map { ub ->
            UserBadgeResponse(
                badge = BadgeResponse(
                    id = ub.badge.id,
                    name = ub.badge.name,
                    description = ub.badge.description,
                    iconUrl = ub.badge.iconUrl,
                    criteria = ub.badge.criteria
                ),
                awardedAt = ub.awardedAt
            )
        }
    }

    fun getAllBadges(): List<BadgeResponse> {
        return badgeRepository.findAll().map {
            BadgeResponse(
                id = it.id,
                name = it.name,
                description = it.description,
                iconUrl = it.iconUrl,
                criteria = it.criteria
            )
        }
    }

    fun seedBadges() {
        val badges = listOf(
            BadgeDefinition("FIRST_QUIZ", "Quiz Beginner", "Completed your first quiz", "/badges/first_quiz.png", "QUIZ_COUNT_1"),
            BadgeDefinition("QUIZ_10", "Quiz Enthusiast", "Completed 10 quizzes", "/badges/quiz_10.png", "QUIZ_COUNT_10"),
            BadgeDefinition("QUIZ_50", "Quiz Master", "Completed 50 quizzes", "/badges/quiz_50.png", "QUIZ_COUNT_50"),
            BadgeDefinition("FIRST_VIDEO", "First View", "Watched your first video", "/badges/first_video.png", "VIDEO_WATCH_1"),
            BadgeDefinition("LEVEL_5", "Rising Star", "Reached level 5", "/badges/level_5.png", "LEVEL_5"),
            BadgeDefinition("LEVEL_10", "Knowledge Seeker", "Reached level 10", "/badges/level_10.png", "LEVEL_10"),
            BadgeDefinition("PERFECT_SCORE", "Perfect Score", "Got 100% on a quiz", "/badges/perfect_score.png", "PERFECT_SCORE")
        )

        badges.forEach { badge ->
            badgeRepository.seedBadge(badge.id, badge.name, badge.description, badge.iconUrl, badge.criteria)
        }
    }

    private fun handleQuizCompleted(userId: String, event: BadgeEvent.QuizCompleted) {
        val attemptCount = getQuizAttemptCount(userId)
        if (attemptCount >= 1) awardIfNew(userId, "FIRST_QUIZ")
        if (attemptCount >= 10) awardIfNew(userId, "QUIZ_10")
        if (attemptCount >= 50) awardIfNew(userId, "QUIZ_50")
        if (event.isPerfectScore) awardIfNew(userId, "PERFECT_SCORE")
    }

    private fun handleLevelUp(userId: String, event: BadgeEvent.LevelUp) {
        if (event.newLevel >= 5) awardIfNew(userId, "LEVEL_5")
        if (event.newLevel >= 10) awardIfNew(userId, "LEVEL_10")
    }

    private fun handleVideoWatched(userId: String) {
        awardIfNew(userId, "FIRST_VIDEO")
    }

    private fun awardIfNew(userId: String, badgeId: String) {
        badgeRepository.awardBadge(userId, badgeId)
    }

    private fun getQuizAttemptCount(userId: String): Long = transaction {
        QuizAttemptsTable.selectAll()
            .where { QuizAttemptsTable.userId eq userId }
            .count()
    }

    private data class BadgeDefinition(
        val id: String,
        val name: String,
        val description: String,
        val iconUrl: String,
        val criteria: String
    )
}
