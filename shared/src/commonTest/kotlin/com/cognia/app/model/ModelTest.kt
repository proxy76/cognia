package com.cognia.app.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ModelTest {

    // Enum completeness tests

    @Test
    fun userRoleHasExpectedValues() {
        assertEquals(5, UserRole.entries.size)
    }

    @Test
    fun contentStatusHasExpectedValues() {
        assertEquals(7, ContentStatus.entries.size)
    }

    @Test
    fun quizTypeHasExpectedValues() {
        assertEquals(2, QuizType.entries.size)
    }

    @Test
    fun difficultyHasExpectedValues() {
        assertEquals(3, Difficulty.entries.size)
    }

    @Test
    fun friendshipStatusHasExpectedValues() {
        assertEquals(3, FriendshipStatus.entries.size)
    }

    @Test
    fun chatMessageTypeHasExpectedValues() {
        assertEquals(2, ChatMessageType.entries.size)
    }

    @Test
    fun sharedContentTypeHasExpectedValues() {
        assertEquals(2, SharedContentType.entries.size)
    }

    @Test
    fun contentEntityTypeHasExpectedValues() {
        assertEquals(2, ContentEntityType.entries.size)
    }

    @Test
    fun moderationDecisionHasExpectedValues() {
        assertEquals(2, ModerationDecision.entries.size)
    }

    @Test
    fun reportReasonHasExpectedValues() {
        assertEquals(2, ReportReason.entries.size)
    }

    @Test
    fun reportStatusHasExpectedValues() {
        assertEquals(3, ReportStatus.entries.size)
    }

    @Test
    fun licenseRequestStatusHasExpectedValues() {
        assertEquals(3, LicenseRequestStatus.entries.size)
    }

    @Test
    fun leaderboardTypeHasExpectedValues() {
        assertEquals(2, LeaderboardType.entries.size)
    }

    @Test
    fun notificationTypeHasExpectedValues() {
        assertEquals(11, NotificationType.entries.size)
    }

    // Leveling logic tests

    @Test
    fun levelForPointsAtZero() {
        assertEquals(1, Leveling.levelForPoints(0))
    }

    @Test
    fun levelForPointsAt99() {
        assertEquals(1, Leveling.levelForPoints(99))
    }

    @Test
    fun levelForPointsAt100() {
        assertEquals(2, Leveling.levelForPoints(100))
    }

    @Test
    fun levelForPointsAt250() {
        assertEquals(3, Leveling.levelForPoints(250))
    }

    @Test
    fun pointsForLevel1() {
        assertEquals(0L, Leveling.pointsForLevel(1))
    }

    @Test
    fun pointsForLevel2() {
        assertEquals(100L, Leveling.pointsForLevel(2))
    }

    @Test
    fun pointsForLevel5() {
        assertEquals(400L, Leveling.pointsForLevel(5))
    }

    @Test
    fun pointsPerLevelConstant() {
        assertEquals(100, Leveling.POINTS_PER_LEVEL)
    }

    // Difficulty points tests

    @Test
    fun difficultyEasyPoints() {
        assertEquals(10, Difficulty.EASY.pointsPerCorrectAnswer)
    }

    @Test
    fun difficultyMediumPoints() {
        assertEquals(20, Difficulty.MEDIUM.pointsPerCorrectAnswer)
    }

    @Test
    fun difficultyHardPoints() {
        assertEquals(30, Difficulty.HARD.pointsPerCorrectAnswer)
    }

    @Test
    fun difficultyBasePoints() {
        assertEquals(10, Difficulty.BASE_POINTS)
    }
}
