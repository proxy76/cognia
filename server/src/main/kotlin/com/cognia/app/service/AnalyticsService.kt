package com.cognia.app.service

import com.cognia.app.database.ContentViewsTable
import com.cognia.app.database.QuizAttemptsTable
import com.cognia.app.database.QuizzesTable
import com.cognia.app.database.VideosTable
import com.cognia.app.dto.analytics.CreatorAnalyticsResponse
import com.cognia.app.dto.analytics.VideoStatsResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class AnalyticsService {

    fun getCreatorAnalytics(creatorId: String): CreatorAnalyticsResponse = transaction {
        // Get creator's videos
        val creatorVideos = VideosTable.selectAll()
            .where { VideosTable.creatorId eq creatorId }
            .map { it[VideosTable.id] to it[VideosTable.title] }

        val videoIds = creatorVideos.map { it.first }

        // Total views on creator's videos
        val totalViews = if (videoIds.isEmpty()) 0L else {
            ContentViewsTable.selectAll()
                .where { (ContentViewsTable.contentId inList videoIds) and (ContentViewsTable.contentType eq "VIDEO") }
                .count()
        }

        // View counts per video
        val videoStats = if (videoIds.isEmpty()) emptyList() else {
            val viewCounts = ContentViewsTable
                .select(ContentViewsTable.contentId, ContentViewsTable.contentId.count())
                .where { (ContentViewsTable.contentId inList videoIds) and (ContentViewsTable.contentType eq "VIDEO") }
                .groupBy(ContentViewsTable.contentId)
                .associate { it[ContentViewsTable.contentId] to it[ContentViewsTable.contentId.count()] }

            creatorVideos.map { (videoId, title) ->
                VideoStatsResponse(
                    videoId = videoId,
                    title = title,
                    viewCount = viewCounts[videoId] ?: 0L
                )
            }
        }

        // Get creator's quizzes
        val creatorQuizIds = QuizzesTable.selectAll()
            .where { QuizzesTable.creatorId eq creatorId }
            .map { it[QuizzesTable.id] }

        // Total quiz attempts
        val totalQuizAttempts = if (creatorQuizIds.isEmpty()) 0L else {
            QuizAttemptsTable.selectAll()
                .where { QuizAttemptsTable.quizId inList creatorQuizIds }
                .count()
        }

        // Average quiz score
        val averageQuizScore = if (creatorQuizIds.isEmpty()) 0.0 else {
            val attempts = QuizAttemptsTable.selectAll()
                .where { QuizAttemptsTable.quizId inList creatorQuizIds }
                .map { row ->
                    val score = row[QuizAttemptsTable.score]
                    val total = row[QuizAttemptsTable.totalQuestions]
                    if (total > 0) score.toDouble() / total.toDouble() * 100.0 else 0.0
                }
            if (attempts.isEmpty()) 0.0 else attempts.average()
        }

        CreatorAnalyticsResponse(
            totalViews = totalViews,
            totalQuizAttempts = totalQuizAttempts,
            averageQuizScore = averageQuizScore,
            videoStats = videoStats
        )
    }
}
