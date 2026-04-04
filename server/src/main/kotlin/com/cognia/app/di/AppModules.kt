package com.cognia.app.di

import com.cognia.app.config.AppConfig
import com.cognia.app.repository.UserRepository
import com.cognia.app.service.AuthService
import org.koin.dsl.module

// Empty module stubs for each feature area — will be populated as features are implemented

val authModule = module {
    single { AppConfig.fromEnvironment() }
    single { UserRepository() }
    single { AuthService(get(), get()) }
}

val userModule = module {
    // UserService, UserProfileRepository
}

val onboardingModule = module {
    // OnboardingService, CategoryRepository
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
