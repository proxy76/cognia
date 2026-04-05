package com.cognia.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cognia.app.ui.auth.AuthViewModel
import com.cognia.app.ui.auth.LoginScreen
import com.cognia.app.ui.auth.RegisterScreen
import com.cognia.app.ui.auth.WelcomeScreen
import com.cognia.app.ui.create.CreateScreen
import com.cognia.app.ui.reel.ReelPlayerScreen
import com.cognia.app.ui.reel.ReelViewModel
import com.cognia.app.ui.chat.ChatConversationScreen
import com.cognia.app.ui.chat.ChatConversationViewModel
import com.cognia.app.ui.chat.ChatListScreen as ChatListScreenNew
import com.cognia.app.ui.chat.ChatListViewModel
import com.cognia.app.ui.screens.HomeScreen
import com.cognia.app.ui.onboarding.OnboardingScreen
import com.cognia.app.ui.onboarding.OnboardingViewModel
import com.cognia.app.ui.screens.PlaceholderScreen
import com.cognia.app.ui.quiz.QuizScreen
import com.cognia.app.ui.profile.ProfileScreen
import com.cognia.app.ui.profile.ProfileViewModel
import com.cognia.app.ui.feed.FeedScreen
import com.cognia.app.ui.search.SearchScreenContent
import com.cognia.app.ui.notification.NotificationScreen
import com.cognia.app.ui.notification.NotificationViewModel
import com.cognia.app.ui.analytics.AnalyticsScreen
import com.cognia.app.ui.analytics.AnalyticsViewModel
import com.cognia.app.ui.leaderboard.LeaderboardScreen
import com.cognia.app.ui.leaderboard.LeaderboardViewModel
import com.cognia.app.ui.moderation.ModerationDashboard
import com.cognia.app.ui.theme.DashboardContentMaxWidth
import com.cognia.app.ui.theme.LocalWindowWidthClass
import com.cognia.app.ui.theme.MobileContentMaxWidth
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.SurfaceDark
import com.cognia.app.ui.theme.WindowWidthClass
import com.cognia.app.ui.theme.immersiveRoutes

/** Routes that use a wider layout on desktop (dashboards, analytics). */
private val wideLayoutRoutes = setOf(
    Screen.ModerationDashboard.route,
    Screen.CreatorAnalytics.route,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showNav = currentRoute in bottomNavItems.map { it.screen.route }
    val windowWidthClass = LocalWindowWidthClass.current
    val useRail = showNav && windowWidthClass != WindowWidthClass.COMPACT

    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }

    // Determine layout strategy per route type
    val isWideRoute = currentRoute in wideLayoutRoutes
    val isImmersive = currentRoute in immersiveRoutes
    val maxContentWidth = when {
        isImmersive -> dp_unbound  // Video player handles its own desktop framing
        isWideRoute -> DashboardContentMaxWidth
        windowWidthClass == WindowWidthClass.COMPACT -> dp_unbound
        else -> MobileContentMaxWidth
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Side rail for medium / expanded screens ──
        if (useRail) {
            NavigationRail(
                containerColor = SurfaceDark,
                contentColor = NeonPurple,
                modifier = Modifier.fillMaxHeight(),
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    if (index == 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    val selected = currentRoute == item.screen.route
                    NavigationRailItem(
                        icon = {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        },
                        label = {
                            Text(
                                item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected)
                                    androidx.compose.ui.text.font.FontWeight.SemiBold
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        },
                        selected = selected,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = NeonPurple,
                            selectedTextColor = NeonPurple,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            indicatorColor = NeonPurple.copy(alpha = 0.1f),
                        ),
                        onClick = {
                            navController.navigate(item.screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            }
            VerticalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
            )
        }

        // ── Main content area ──
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.weight(1f),
            bottomBar = {
                // Bottom bar only on compact screens
                if (showNav && !useRail) {
                    NavigationBar(
                        containerColor = SurfaceDark,
                        tonalElevation = 0.dp,
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.screen.route
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected)
                                            androidx.compose.ui.text.font.FontWeight.SemiBold
                                        else
                                            androidx.compose.ui.text.font.FontWeight.Normal
                                    )
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = NeonPurple,
                                    selectedTextColor = NeonPurple,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    indicatorColor = NeonPurple.copy(alpha = 0.1f),
                                ),
                                onClick = {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            },
        ) { paddingValues ->
            // Center-constrain content on wide screens
            // Immersive routes ignore scaffold padding (content goes behind nav bar)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (isImmersive) Modifier else Modifier.padding(paddingValues)),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    modifier = if (maxContentWidth != dp_unbound) {
                        Modifier.widthIn(max = maxContentWidth).fillMaxHeight()
                    } else {
                        Modifier.fillMaxSize()
                    }
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Welcome.route,
                    ) {
                        // Main tabs
                        composable(Screen.Home.route) {
                            val homeReelViewModel: ReelViewModel = viewModel { ReelViewModel() }
                            ReelPlayerScreen(
                                viewModel = homeReelViewModel,
                                onNavigateBack = { /* Already on home, no-op */ },
                                onNavigateToCreator = { userId ->
                                    navController.navigate(Screen.UserProfile.createRoute(userId))
                                },
                                onNavigateToQuiz = { videoId ->
                                    navController.navigate(Screen.QuizScreen.createRoute(videoId))
<<<<<<< Updated upstream
                                }
=======
                                },
                                onNavigateToTopic = { hashtag ->
                                    navController.navigate(Screen.ForYouTopic.createRoute(hashtag))
                                },
                                onNavigateToEli5 = { eli5VideoId ->
                                    navController.navigate(Screen.ReelPlayer.createRoute(eli5VideoId))
                                },
>>>>>>> Stashed changes
                            )
                        }
                        composable(Screen.Search.route) { SearchScreenContent() }
                        composable(Screen.Create.route) { CreateScreen() }
                        composable(Screen.Chat.route) {
                            val chatListViewModel: ChatListViewModel = viewModel { ChatListViewModel() }
                            ChatListScreenNew(
                                viewModel = chatListViewModel,
                                onConversationClick = { conversationId ->
                                    navController.navigate(Screen.ChatConversation.createRoute(conversationId))
                                }
                            )
                        }
                        composable(Screen.Profile.route) {
                            val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel() }
                            ProfileScreen(viewModel = profileViewModel)
                        }

                        // Auth screens
                        composable(Screen.Welcome.route) {
                            WelcomeScreen(
                                onNavigateToRegister = {
                                    authViewModel.clearState()
                                    navController.navigate(Screen.Register.route)
                                },
                                onNavigateToLogin = {
                                    authViewModel.clearState()
                                    navController.navigate(Screen.Login.route)
                                }
                            )
                        }
                        composable(Screen.Login.route) {
                            val authState by authViewModel.state.collectAsState()

                            LaunchedEffect(authState.authSuccess) {
                                val success = authState.authSuccess
                                if (success != null && !success.isNewUser) {
                                    val destination = if (success.role == "ADMIN" || success.role == "MODERATOR") {
                                        Screen.ModerationDashboard.route
                                    } else {
                                        Screen.Home.route
                                    }
                                    navController.navigate(destination) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                    authViewModel.clearState()
                                }
                            }

                            LoginScreen(
                                viewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToRegister = {
                                    authViewModel.clearState()
                                    navController.navigate(Screen.Register.route) {
                                        popUpTo(Screen.Welcome.route)
                                    }
                                }
                            )
                        }
                        composable(Screen.Register.route) {
                            val authState by authViewModel.state.collectAsState()

                            LaunchedEffect(authState.authSuccess) {
                                val success = authState.authSuccess
                                if (success != null && success.isNewUser) {
                                    navController.navigate(Screen.Onboarding.route) {
                                        popUpTo(Screen.Welcome.route) { inclusive = true }
                                    }
                                    authViewModel.clearState()
                                }
                            }

                            RegisterScreen(
                                viewModel = authViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToLogin = {
                                    authViewModel.clearState()
                                    navController.navigate(Screen.Login.route) {
                                        popUpTo(Screen.Welcome.route)
                                    }
                                }
                            )
                        }

                        // Onboarding
                        composable(Screen.Onboarding.route) {
                            val onboardingViewModel: OnboardingViewModel = viewModel { OnboardingViewModel() }
                            OnboardingScreen(
                                viewModel = onboardingViewModel,
                                onComplete = {
                                    navController.navigate(Screen.Home.route) {
                                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Reel player
                        composable(Screen.ReelPlayer.route) { backStackEntry ->
                            val reelViewModel: ReelViewModel = viewModel { ReelViewModel() }
                            ReelPlayerScreen(
                                viewModel = reelViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onNavigateToCreator = { userId ->
                                    navController.navigate(Screen.UserProfile.createRoute(userId))
                                },
                                onNavigateToQuiz = { videoId ->
                                    navController.navigate(Screen.QuizScreen.createRoute(videoId))
<<<<<<< Updated upstream
                                }
=======
                                },
                                onNavigateToTopic = { hashtag ->
                                    navController.navigate(Screen.ForYouTopic.createRoute(hashtag))
                                },
                                onNavigateToEli5 = { eli5VideoId ->
                                    navController.navigate(Screen.ReelPlayer.createRoute(eli5VideoId))
                                },
                            )
                        }

                        // Topic-filtered For You page
                        composable(Screen.ForYouTopic.route) { backStackEntry ->
                            val hashtag = backStackEntry.destination.route
                                ?.removePrefix("fy/")
                                ?: ""
                            FeedScreen(
                                topicFilter = hashtag,
                                onNavigateToCreator = { userId ->
                                    navController.navigate(Screen.UserProfile.createRoute(userId))
                                },
                                onNavigateToQuiz = { videoId ->
                                    navController.navigate(Screen.QuizScreen.createRoute(videoId))
                                },
                                onNavigateToTopic = { tag ->
                                    navController.navigate(Screen.ForYouTopic.createRoute(tag))
                                },
                                onNavigateToEli5 = { eli5VideoId ->
                                    navController.navigate(Screen.ReelPlayer.createRoute(eli5VideoId))
                                },
                                onNavigateBack = { navController.popBackStack() },
>>>>>>> Stashed changes
                            )
                        }

                        // Quiz screen
                        composable(Screen.QuizScreen.route) { backStackEntry ->
                            val quizId = backStackEntry.destination.route
                                ?.substringAfterLast("/")
                                ?: ""
                            QuizScreen(
                                quizId = quizId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Chat conversation
                        composable(Screen.ChatConversation.route) { backStackEntry ->
                            val conversationId = backStackEntry.destination.route
                                ?.substringAfterLast("/")
                                ?: ""
                            val chatConversationViewModel: ChatConversationViewModel = viewModel { ChatConversationViewModel() }
                            ChatConversationScreen(
                                viewModel = chatConversationViewModel,
                                conversationId = conversationId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }

                        // Notifications
                        composable(Screen.Notifications.route) {
                            val notificationViewModel: NotificationViewModel = viewModel { NotificationViewModel() }
                            NotificationScreen(viewModel = notificationViewModel)
                        }

                        // Leaderboard
                        composable(Screen.Leaderboard.route) {
                            val leaderboardViewModel: LeaderboardViewModel = viewModel { LeaderboardViewModel() }
                            LeaderboardScreen(viewModel = leaderboardViewModel)
                        }

                        // Creator Analytics (wide layout)
                        composable(Screen.CreatorAnalytics.route) {
                            val analyticsViewModel: AnalyticsViewModel = viewModel { AnalyticsViewModel() }
                            AnalyticsScreen(viewModel = analyticsViewModel)
                        }

                        // Moderation Dashboard (wide layout, web-only)
                        composable(Screen.ModerationDashboard.route) { ModerationDashboard() }

                        // Settings (placeholder)
                        composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
                    }
                }
            }
        }
    }
}

/** Sentinel for "no max-width constraint". */
private val dp_unbound = 100_000.dp
