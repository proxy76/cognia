package com.cognia.app.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class DatabaseTest {

    private fun withTestDatabase(block: (File) -> Unit) {
        val tempFile = File.createTempFile("cognia-test-", ".db")
        try {
            block(tempFile)
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `init creates database file and all tables`() = withTestDatabase { dbFile ->
        DatabaseFactory.init(dbFile.absolutePath)

        val db = Database.connect("jdbc:sqlite:${dbFile.absolutePath}?foreign_keys=on", driver = "org.sqlite.JDBC")

        transaction(db) {
            // Query sqlite_master for all tables created by SchemaUtils
            val tables = exec("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%';") { rs ->
                buildList {
                    while (rs.next()) {
                        add(rs.getString("name"))
                    }
                }
            } ?: emptyList()

            val expectedTables = listOf(
                "users",
                "user_profiles",
                "user_categories",
                "refresh_tokens",
                "categories",
                "videos",
                "quizzes",
                "quiz_questions",
                "quiz_options",
                "quiz_attempts",
                "follows",
                "friendships",
                "chat_conversations",
                "chat_messages",
                "moderation_reviews",
                "content_reports",
                "strikes",
                "creator_license_requests",
                "badges",
                "user_badges",
                "notifications",
                "content_views",
                "content_shares",
                "search_history"
            )

            for (expected in expectedTables) {
                assertTrue(
                    tables.contains(expected),
                    "Expected table '$expected' to exist. Found tables: $tables"
                )
            }
            assertEquals(expectedTables.size, tables.size, "Expected exactly ${expectedTables.size} tables")
        }
    }

    @Test
    fun `foreign key enforcement rejects orphan child rows`() = withTestDatabase { dbFile ->
        DatabaseFactory.init(dbFile.absolutePath)

        val db = Database.connect("jdbc:sqlite:${dbFile.absolutePath}?foreign_keys=on", driver = "org.sqlite.JDBC")

        transaction(db) {

            // Insert a valid user first to confirm inserts work
            UsersTable.insert {
                it[id] = "user-1"
                it[email] = "test@example.com"
                it[displayName] = "Test User"
                it[role] = "LEARNER"
                it[authProvider] = "EMAIL"
                it[createdAt] = "2025-01-01T00:00:00Z"
                it[updatedAt] = "2025-01-01T00:00:00Z"
            }

            val count = UsersTable.selectAll().count()
            assertEquals(1L, count)

            // Attempting to insert a refresh token with a non-existent user should fail
            assertFailsWith<Exception> {
                RefreshTokensTable.insert {
                    it[id] = "token-1"
                    it[userId] = "non-existent-user"
                    it[tokenHash] = "hash123"
                    it[expiresAt] = "2025-12-31T00:00:00Z"
                    it[createdAt] = "2025-01-01T00:00:00Z"
                }
            }
        }
    }

    @Test
    fun `database init creates parent directories`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-nested/subdir")
        val dbFile = File(tempDir, "test.db")
        try {
            DatabaseFactory.init(dbFile.absolutePath)
            assertTrue(dbFile.exists(), "Database file should be created")
            assertTrue(tempDir.exists(), "Parent directories should be created")
        } finally {
            dbFile.delete()
            tempDir.delete()
            tempDir.parentFile?.delete()
        }
    }
}
