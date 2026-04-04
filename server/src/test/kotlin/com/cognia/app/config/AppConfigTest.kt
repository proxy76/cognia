package com.cognia.app.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AppConfigTest {

    @Test
    fun `fromEnvironment returns a fully populated config`() {
        val config = AppConfig.fromEnvironment()
        assertNotNull(config.server)
        assertNotNull(config.database)
        assertNotNull(config.jwt)
        assertNotNull(config.video)
        assertNotNull(config.oauth)
        assertNotNull(config.ai)
        assertNotNull(config.ffmpeg)
    }

    @Test
    fun `server config has correct defaults`() {
        val server = ServerConfig.fromEnvironment()
        assertEquals(8080, server.port)
        assertEquals("0.0.0.0", server.host)
    }

    @Test
    fun `database config has correct defaults`() {
        val db = DatabaseConfig.fromEnvironment()
        assertEquals("jdbc:postgresql://localhost:5432/cognia", db.url)
        assertEquals("org.postgresql.Driver", db.driver)
        assertEquals("cognia", db.user)
        assertEquals("cognia", db.password)
        assertEquals(10, db.maxPoolSize)
    }

    @Test
    fun `jwt config has correct defaults`() {
        val jwt = JwtConfig.fromEnvironment()
        assertEquals("dev-secret-change-in-production", jwt.secret)
        assertEquals("cognia-dev", jwt.issuer)
        assertEquals(3_600_000L, jwt.expirationMs)
        assertEquals(2_592_000_000L, jwt.refreshExpirationMs)
    }

    @Test
    fun `video config has correct defaults`() {
        val video = VideoConfig.fromEnvironment()
        assertEquals("./data/videos/raw", video.rawPath)
        assertEquals("./data/videos/processed", video.processedPath)
        assertEquals("./data/videos/thumbnails", video.thumbnailPath)
        assertEquals(500, video.maxSizeMb)
    }

    @Test
    fun `oauth config defaults to empty strings`() {
        val oauth = OAuthConfig.fromEnvironment()
        assertEquals("", oauth.googleClientId)
        assertEquals("", oauth.appleClientId)
        assertEquals("", oauth.appleTeamId)
        assertEquals("", oauth.appleKeyId)
    }

    @Test
    fun `ai config has correct defaults`() {
        val ai = AiConfig.fromEnvironment()
        assertEquals("claude-sonnet-4-20250514", ai.anthropicModel)
    }

    @Test
    fun `ffmpeg config has correct default path`() {
        val ffmpeg = FfmpegConfig.fromEnvironment()
        assertEquals("/opt/homebrew/bin/ffmpeg", ffmpeg.path)
    }
}
