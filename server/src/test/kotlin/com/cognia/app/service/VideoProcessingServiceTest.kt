package com.cognia.app.service

import com.cognia.app.config.*
import com.cognia.app.database.DatabaseFactory
import com.cognia.app.database.VideosTable
import com.cognia.app.database.UsersTable
import com.cognia.app.database.CategoriesTable
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class VideoProcessingServiceTest {

    private fun createTestConfig(tempDir: File): AppConfig {
        val processedDir = File(tempDir, "processed")
        val thumbnailDir = File(tempDir, "thumbnails")
        return AppConfig(
            server = ServerConfig(8080, "0.0.0.0"),
            database = DatabaseConfig("jdbc:sqlite:test.db", "org.sqlite.JDBC", "", "", 1),
            jwt = JwtConfig("test-secret", "test-issuer", 3600000L, 2592000000L),
            video = VideoConfig(
                rawPath = File(tempDir, "raw").absolutePath,
                processedPath = processedDir.absolutePath,
                thumbnailPath = thumbnailDir.absolutePath,
                maxSizeMb = 500
            ),
            oauth = OAuthConfig("", "", "", ""),
            ai = AiConfig("", "claude-sonnet-4-20250514", "claude-haiku-4-5-20251001", 1024),
            moderation = ModerationConfig(0.95, 0.7),
            ffmpeg = FfmpegConfig("/usr/bin/false")
        )
    }

    private fun setupTestDb(): File {
        val testDbFile = File.createTempFile("cognia-proc-test-", ".db")
        testDbFile.deleteOnExit()
        DatabaseFactory.init(testDbFile.absolutePath)
        return testDbFile
    }

    private fun insertTestVideo(videoId: String, rawPath: String) {
        transaction {
            CategoriesTable.insert {
                it[id] = "cat-1"
                it[name] = "Test Category"
                it[slug] = "test-category"
            }
            UsersTable.insert {
                it[id] = "user-1"
                it[email] = "test@example.com"
                it[passwordHash] = "hash"
                it[displayName] = "Test User"
                it[role] = "CREATOR"
                it[authProvider] = "EMAIL"
                it[createdAt] = Instant.now().toString()
                it[updatedAt] = Instant.now().toString()
            }
            VideosTable.insert {
                it[id] = videoId
                it[creatorId] = "user-1"
                it[title] = "Test Video"
                it[categoryId] = "cat-1"
                it[rawFilePath] = rawPath
                it[status] = "UPLOADED"
                it[createdAt] = Instant.now().toString()
                it[updatedAt] = Instant.now().toString()
            }
        }
    }

    @Test
    fun `updateVideoStatus marks video as PROCESSING_FAILED`() {
        val dbFile = setupTestDb()
        insertTestVideo("vid-1", "/tmp/test.mp4")

        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-${System.nanoTime()}")
        tempDir.mkdirs()

        try {
            val config = createTestConfig(tempDir)
            val service = FfmpegVideoProcessingService(config)
            service.updateVideoStatus("vid-1", "PROCESSING_FAILED")

            val status = transaction {
                VideosTable.selectAll().where { VideosTable.id eq "vid-1" }
                    .single()[VideosTable.status]
            }
            assertEquals("PROCESSING_FAILED", status)
        } finally {
            tempDir.deleteRecursively()
            dbFile.delete()
        }
    }

    @Test
    fun `updateVideoRecord updates videoUrl and thumbnailUrl and status`() {
        val dbFile = setupTestDb()
        insertTestVideo("vid-2", "/tmp/test.mp4")

        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-${System.nanoTime()}")
        tempDir.mkdirs()

        try {
            val config = createTestConfig(tempDir)
            val service = FfmpegVideoProcessingService(config)
            service.updateVideoRecord("vid-2", "/processed/vid-2.mp4", "/thumbnails/vid-2.jpg")

            val row = transaction {
                VideosTable.selectAll().where { VideosTable.id eq "vid-2" }.single()
            }
            assertEquals("/processed/vid-2.mp4", row[VideosTable.videoUrl])
            assertEquals("/thumbnails/vid-2.jpg", row[VideosTable.thumbnailUrl])
            assertEquals("PROCESSED", row[VideosTable.status])
        } finally {
            tempDir.deleteRecursively()
            dbFile.delete()
        }
    }

    @Test
    fun `runFfmpeg returns non-zero for invalid command`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-${System.nanoTime()}")
        tempDir.mkdirs()

        try {
            val config = createTestConfig(tempDir)
            val service = FfmpegVideoProcessingService(config)
            val result = service.runFfmpeg("/usr/bin/false")
            assertEquals(1, result)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `runFfmpeg returns zero for successful command`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-${System.nanoTime()}")
        tempDir.mkdirs()

        try {
            val config = createTestConfig(tempDir)
            val service = FfmpegVideoProcessingService(config)
            val result = service.runFfmpeg("/usr/bin/true")
            assertEquals(0, result)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `updateVideoStatus sets PROCESSING status`() {
        val dbFile = setupTestDb()
        insertTestVideo("vid-3", "/tmp/test.mp4")

        val tempDir = File(System.getProperty("java.io.tmpdir"), "cognia-test-${System.nanoTime()}")
        tempDir.mkdirs()

        try {
            val config = createTestConfig(tempDir)
            val service = FfmpegVideoProcessingService(config)
            service.updateVideoStatus("vid-3", "PROCESSING")

            val status = transaction {
                VideosTable.selectAll().where { VideosTable.id eq "vid-3" }
                    .single()[VideosTable.status]
            }
            assertEquals("PROCESSING", status)
        } finally {
            tempDir.deleteRecursively()
            dbFile.delete()
        }
    }
}
