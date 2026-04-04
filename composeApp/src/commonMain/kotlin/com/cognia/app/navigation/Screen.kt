package com.cognia.app.navigation

sealed class Screen(val route: String) {
    // Main tabs
    data object Home : Screen("home")
    data object Search : Screen("search")
    data object Create : Screen("create")
    data object Chat : Screen("chat")
    data object Profile : Screen("profile")

    // Auth
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")

    // Onboarding
    data object Onboarding : Screen("onboarding")

    // Detail screens
    data object ReelPlayer : Screen("reel/{videoId}") {
        fun createRoute(videoId: String) = "reel/$videoId"
    }
    data object QuizScreen : Screen("quiz/{quizId}") {
        fun createRoute(quizId: String) = "quiz/$quizId"
    }
    data object UserProfile : Screen("user/{userId}") {
        fun createRoute(userId: String) = "user/$userId"
    }
    data object ChatConversation : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    data object Notifications : Screen("notifications")
    data object Leaderboard : Screen("leaderboard")
    data object CreatorAnalytics : Screen("analytics")
    data object Settings : Screen("settings")
}
