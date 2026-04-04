package com.cognia.app.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object DatabaseFactory {
    fun init(dbPath: String) {
        // Ensure parent directory exists
        File(dbPath).parentFile?.mkdirs()

        val database = Database.connect(
            url = "jdbc:sqlite:$dbPath?foreign_keys=on",
            driver = "org.sqlite.JDBC"
        )

        transaction(database) {

            // Create all tables
            SchemaUtils.create(
                UsersTable,
                UserProfilesTable,
                CategoriesTable,
                UserCategoriesTable,
                RefreshTokensTable,
                VideosTable,
                QuizzesTable,
                QuizQuestionsTable,
                QuizOptionsTable,
                QuizAttemptsTable,
                FollowsTable,
                FriendshipsTable,
                ChatConversationsTable,
                ChatMessagesTable,
                ModerationReviewsTable,
                ContentReportsTable,
                StrikesTable,
                CreatorLicenseRequestsTable,
                BadgesTable,
                UserBadgesTable,
                NotificationsTable,
                ContentViewsTable,
                ContentSharesTable,
                SearchHistoryTable
            )
        }
    }
}
