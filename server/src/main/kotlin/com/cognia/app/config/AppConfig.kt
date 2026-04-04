package com.cognia.app.config

data class AppConfig(
    val server: ServerConfig,
    val database: DatabaseConfig,
    val jwt: JwtConfig,
    val video: VideoConfig,
    val oauth: OAuthConfig,
    val ai: AiConfig,
    val moderation: ModerationConfig,
    val ffmpeg: FfmpegConfig
) {
    companion object {
        fun fromEnvironment(): AppConfig = AppConfig(
            server = ServerConfig.fromEnvironment(),
            database = DatabaseConfig.fromEnvironment(),
            jwt = JwtConfig.fromEnvironment(),
            video = VideoConfig.fromEnvironment(),
            oauth = OAuthConfig.fromEnvironment(),
            ai = AiConfig.fromEnvironment(),
            moderation = ModerationConfig.fromEnvironment(),
            ffmpeg = FfmpegConfig.fromEnvironment()
        )
    }
}

data class ServerConfig(
    val port: Int,
    val host: String
) {
    companion object {
        fun fromEnvironment() = ServerConfig(
            port = System.getenv("COGNIA_PORT")?.toIntOrNull() ?: 8080,
            host = System.getenv("COGNIA_HOST") ?: "0.0.0.0"
        )
    }
}

data class DatabaseConfig(
    val path: String
) {
    companion object {
        fun fromEnvironment() = DatabaseConfig(
            path = System.getenv("COGNIA_DB_PATH") ?: "./data/cognia-dev.db"
        )
    }
}

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val expirationMs: Long,
    val refreshExpirationMs: Long
) {
    companion object {
        fun fromEnvironment() = JwtConfig(
            secret = System.getenv("COGNIA_JWT_SECRET") ?: "dev-secret-change-in-production",
            issuer = System.getenv("COGNIA_JWT_ISSUER") ?: "cognia-dev",
            expirationMs = System.getenv("COGNIA_JWT_EXPIRATION_MS")?.toLongOrNull() ?: 3_600_000L,
            refreshExpirationMs = System.getenv("COGNIA_JWT_REFRESH_EXPIRATION_MS")?.toLongOrNull() ?: 2_592_000_000L
        )
    }
}

data class VideoConfig(
    val rawPath: String,
    val processedPath: String,
    val thumbnailPath: String,
    val maxSizeMb: Int
) {
    companion object {
        fun fromEnvironment() = VideoConfig(
            rawPath = System.getenv("COGNIA_VIDEO_RAW_PATH") ?: "./data/videos/raw",
            processedPath = System.getenv("COGNIA_VIDEO_PROCESSED_PATH") ?: "./data/videos/processed",
            thumbnailPath = System.getenv("COGNIA_VIDEO_THUMBNAIL_PATH") ?: "./data/videos/thumbnails",
            maxSizeMb = System.getenv("COGNIA_VIDEO_MAX_SIZE_MB")?.toIntOrNull() ?: 500
        )
    }
}

data class OAuthConfig(
    val googleClientId: String,
    val appleClientId: String,
    val appleTeamId: String,
    val appleKeyId: String
) {
    companion object {
        fun fromEnvironment() = OAuthConfig(
            googleClientId = System.getenv("COGNIA_GOOGLE_CLIENT_ID") ?: "",
            appleClientId = System.getenv("COGNIA_APPLE_CLIENT_ID") ?: "",
            appleTeamId = System.getenv("COGNIA_APPLE_TEAM_ID") ?: "",
            appleKeyId = System.getenv("COGNIA_APPLE_KEY_ID") ?: ""
        )
    }
}

data class AiConfig(
    val anthropicApiKey: String,
    val anthropicModel: String,
    val anthropicFastModel: String,
    val maxTokens: Int
) {
    companion object {
        fun fromEnvironment() = AiConfig(
            anthropicApiKey = System.getenv("COGNIA_ANTHROPIC_API_KEY") ?: "",
            anthropicModel = System.getenv("COGNIA_ANTHROPIC_MODEL") ?: "claude-sonnet-4-20250514",
            anthropicFastModel = System.getenv("COGNIA_ANTHROPIC_FAST_MODEL") ?: "claude-haiku-4-5-20251001",
            maxTokens = System.getenv("COGNIA_ANTHROPIC_MAX_TOKENS")?.toIntOrNull() ?: 1024
        )
    }
}

data class ModerationConfig(
    val autoRejectThreshold: Double,
    val reviewThreshold: Double
) {
    companion object {
        fun fromEnvironment() = ModerationConfig(
            autoRejectThreshold = System.getenv("COGNIA_MODERATION_AUTO_REJECT_THRESHOLD")?.toDoubleOrNull() ?: 0.95,
            reviewThreshold = System.getenv("COGNIA_MODERATION_REVIEW_THRESHOLD")?.toDoubleOrNull() ?: 0.7
        )
    }
}

data class FfmpegConfig(
    val path: String
) {
    companion object {
        fun fromEnvironment() = FfmpegConfig(
            path = System.getenv("COGNIA_FFMPEG_PATH") ?: "/opt/homebrew/bin/ffmpeg"
        )
    }
}
