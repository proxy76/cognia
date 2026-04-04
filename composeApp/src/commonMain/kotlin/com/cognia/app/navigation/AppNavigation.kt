package com.cognia.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cognia.app.ui.screens.ChatListScreen
import com.cognia.app.ui.screens.CreateScreen
import com.cognia.app.ui.screens.HomeScreen
import com.cognia.app.ui.screens.PlaceholderScreen
import com.cognia.app.ui.screens.ProfileScreen
import com.cognia.app.ui.screens.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in bottomNavItems.map { it.screen.route }

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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
        ) {
            // Main tabs
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.Search.route) { SearchScreen() }
            composable(Screen.Create.route) { CreateScreen() }
            composable(Screen.Chat.route) { ChatListScreen() }
            composable(Screen.Profile.route) { ProfileScreen() }

            // Auth (placeholder)
            composable(Screen.Welcome.route) { PlaceholderScreen("Welcome") }
            composable(Screen.Login.route) { PlaceholderScreen("Login") }
            composable(Screen.Register.route) { PlaceholderScreen("Register") }

            // Other screens (placeholder)
            composable(Screen.Notifications.route) { PlaceholderScreen("Notifications") }
            composable(Screen.Leaderboard.route) { PlaceholderScreen("Leaderboard") }
            composable(Screen.Settings.route) { PlaceholderScreen("Settings") }
        }
    }
}
