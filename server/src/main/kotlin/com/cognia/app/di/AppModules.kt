package com.cognia.app.di

import com.cognia.app.config.AppConfig
import com.cognia.app.repository.CategoryRepository
import com.cognia.app.repository.PreferenceRepository
import com.cognia.app.repository.RefreshTokenRepository
import com.cognia.app.repository.UserRepository
import com.cognia.app.repository.UserProfileRepository
import com.cognia.app.service.AuthService
import com.cognia.app.service.CategoryService
import com.cognia.app.service.PreferenceService
import com.cognia.app.service.UserProfileService
import com.cognia.app.service.AppleTokenVerifier
import com.cognia.app.service.DevAppleTokenVerifier
import com.cognia.app.service.DevGoogleTokenVerifier
import com.cognia.app.service.GoogleTokenVerifier
import com.cognia.app.service.AnthropicRecommendationClient
import com.cognia.app.service.OAuthService
import com.cognia.app.service.OnboardingService
import com.cognia.app.service.RecommendationClient
import com.cognia.app.service.FfmpegVideoProcessingService
import com.cognia.app.service.VideoProcessingQueue
import com.cognia.app.service.VideoProcessingService
import com.cognia.app.service.VideoService
import com.cognia.app.service.LevelService
import com.cognia.app.service.BadgeService
import com.cognia.app.repository.QuizAttemptRepository
import com.cognia.app.repository.QuizRepository
import com.cognia.app.repository.VideoRepository
import com.cognia.app.repository.BadgeRepository
import com.cognia.app.service.QuizService
import com.cognia.app.service.ScoringService
import com.cognia.app.repository.FeedRepository
import com.cognia.app.service.FeedService
import com.cognia.app.service.TrackingService
import com.cognia.app.service.SearchService
import com.cognia.app.repository.FollowRepository
import com.cognia.app.repository.FriendshipRepository
import com.cognia.app.repository.ChatRepository
import com.cognia.app.service.FollowService
import com.cognia.app.service.FriendshipService
import com.cognia.app.service.ChatService
import com.cognia.app.repository.NotificationRepository
import com.cognia.app.service.NotificationService
import com.cognia.app.service.AnalyticsService
import com.cognia.app.repository.LicenseRepository
import com.cognia.app.repository.ModerationRepository
import com.cognia.app.repository.ReportRepository
import com.cognia.app.repository.StrikeRepository
import com.cognia.app.service.LicenseService
import com.cognia.app.service.ModerationService
import com.cognia.app.service.ReportService
import com.cognia.app.service.SeedService
import com.cognia.app.service.StrikeService
import com.cognia.app.service.AiCategorizationService
import com.cognia.app.service.AiModerationService
import com.cognia.app.service.AiQuizGenerationService
import com.cognia.app.service.BlockService
import com.cognia.app.service.AuditLogService
import com.cognia.app.service.AdminService
import com.cognia.app.repository.BlockRepository
import com.cognia.app.repository.AuditLogRepository
import org.koin.dsl.module

// Empty module stubs for each feature area — will be populated as features are implemented

val authModule = module {
    single { AppConfig.fromEnvironment() }
    single { UserRepository() }
    single { RefreshTokenRepository() }
    single { AuthService(get(), get(), get()) }
    single<GoogleTokenVerifier> { DevGoogleTokenVerifier() }
    single<AppleTokenVerifier> { DevAppleTokenVerifier() }
    single { OAuthService(get(), get()) }
}

val userModule = module {
    single { UserProfileRepository() }
    single { UserProfileService(get()) }
}

val onboardingModule = module {
    single { CategoryRepository() }
    single { CategoryService(get()) }
    single { PreferenceRepository() }
    single { PreferenceService(get(), get()) }
    single<RecommendationClient> { AnthropicRecommendationClient(get()) }
    single { AnthropicRecommendationClient(get()) }
    single { OnboardingService(get()) }
    single { AiCategorizationService(get()) }
    single { AiQuizGenerationService(get()) }
}

val contentModule = module {
    single { VideoRepository() }
    single { VideoService(get(), getOrNull(), getOrNull()) }
    single<VideoProcessingService> { FfmpegVideoProcessingService(get()) }
    single { VideoProcessingQueue(get()) }
}

val quizModule = module {
    single { QuizRepository() }
    single { QuizAttemptRepository() }
    single { QuizService(get()) }
    single { ScoringService(get(), get()) }
}

val feedModule = module {
    single { FeedRepository() }
    single { FeedService(get(), get()) }
    single { TrackingService() }
}

val socialModule = module {
    single { FollowRepository() }
    single { FollowService(get()) }
    single { FriendshipRepository() }
    single { FriendshipService(get()) }
}

val chatModule = module {
    single { ChatRepository() }
    single { ChatService(get(), get()) }
}

val moderationModule = module {
    single { ModerationRepository() }
    single { ReportRepository() }
    single { StrikeRepository() }
    single { BlockRepository() }
    single { AuditLogRepository() }
    single { ModerationService(get(), get()) }
    single { ReportService(get(), get()) }
    single { StrikeService(get(), get()) }
    single { BlockService(get()) }
    single { AuditLogService(get()) }
    single { AdminService(get()) }
    single { AiModerationService(get(), get()) }
}

val licensingModule = module {
    single { LicenseRepository() }
    single { LicenseService(get(), get()) }
}

val gamificationModule = module {
    single { BadgeRepository() }
    single { LevelService() }
    single { BadgeService(get()) }
}

val notificationModule = module {
    single { NotificationRepository() }
    single { NotificationService(get()) }
}

val analyticsModule = module {
    single { AnalyticsService() }
}

val searchModule = module {
    single { SearchService() }
}

val seedModule = module {
    single { SeedService() }
}

val allModules = listOf(
    authModule,
    userModule,
    onboardingModule,
    contentModule,
    quizModule,
    feedModule,
    socialModule,
    chatModule,
    moderationModule,
    licensingModule,
    gamificationModule,
    notificationModule,
    analyticsModule,
    searchModule,
    seedModule
)
