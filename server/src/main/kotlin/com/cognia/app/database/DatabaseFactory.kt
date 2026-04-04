package com.cognia.app.database

import com.cognia.app.config.DatabaseConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    /**
     * Production initializer: connects to PostgreSQL via HikariCP and runs Flyway migrations.
     */
    fun init(config: DatabaseConfig) {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            driverClassName = config.driver
            username = config.user
            password = config.password
            maximumPoolSize = config.maxPoolSize
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        }
        val dataSource = HikariDataSource(hikariConfig)

        // Run Flyway migrations
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()

        // Connect Exposed to the pooled datasource
        Database.connect(dataSource)
    }

    /**
     * Test/SQLite initializer: connects directly to a SQLite file and creates tables via SchemaUtils.
     * Used by test suites that still run against SQLite.
     */
    fun init(dbPath: String) {
        val separator = if (dbPath.contains("?")) "&" else "?"
        Database.connect(
            url = "jdbc:sqlite:$dbPath${separator}foreign_keys=on",
            driver = "org.sqlite.JDBC"
        )

        transaction {
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
                UserBlocksTable,
                ModerationAuditLogTable,
                ContentViewsTable,
                ContentSharesTable,
                SearchHistoryTable
            )
        }
    }
}
