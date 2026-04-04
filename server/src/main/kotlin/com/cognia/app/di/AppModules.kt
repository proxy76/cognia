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
    single { OnboardingService(get()) }
}

val contentModule = module {
    // ContentService, VideoRepository, VideoProcessingService
}

val quizModule = module {
    // QuizService, QuizRepository
}

val feedModule = module {
    // FeedService, FeedRepository
}

val socialModule = module {
    // FollowService, FriendshipService, FollowRepository, FriendshipRepository
}

val chatModule = module {
    // ChatService, ChatRepository
}

val moderationModule = module {
    // ModerationService, ModerationRepository, StrikeService
}

val licensingModule = module {
    // LicensingService, LicenseRequestRepository
}

val gamificationModule = module {
    // GamificationService, BadgeService, LeaderboardService
}

val notificationModule = module {
    // NotificationService, NotificationRepository
}

val analyticsModule = module {
    // AnalyticsService, AnalyticsRepository
}

val searchModule = module {
    // SearchService, SearchRepository
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
    searchModule
)
