package com.cognia.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cognia.app.ui.auth.AuthViewModel
import com.cognia.app.ui.auth.LoginScreen
import com.cognia.app.ui.auth.RegisterScreen
import com.cognia.app.ui.auth.WelcomeScreen
import com.cognia.app.ui.screens.ChatListScreen
import com.cognia.app.ui.screens.CreateScreen
import com.cognia.app.ui.screens.HomeScreen
import com.cognia.app.ui.onboarding.OnboardingScreen
import com.cognia.app.ui.onboarding.OnboardingViewModel
import com.cognia.app.ui.screens.PlaceholderScreen
import com.cognia.app.ui.profile.ProfileScreen
import com.cognia.app.ui.profile.ProfileViewModel
import com.cognia.app.ui.screens.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

    val authViewModel: AuthViewModel = viewModel { AuthViewModel() }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.screen.route,
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
        NavHost(
            navController = navController,
            startDestination = Screen.Welcome.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            // Main tabs
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Create.route) { CreateScreen() }
            composable(Screen.Chat.route) { ChatListScreen() }
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
                        navController.navigate(Screen.Home.route) {
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

            // Other screens (placeholder)
            composable(Screen.Notifications.route) { PlaceholderScreen("Notifications") }
            composable(Screen.Leaderboard.route) { PlaceholderScreen("Leaderboard") }
            composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
        }
    }
}
