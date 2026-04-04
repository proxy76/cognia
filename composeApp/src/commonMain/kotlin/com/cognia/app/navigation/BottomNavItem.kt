package com.cognia.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Default.Search),
    BottomNavItem(Screen.Create, "Create", Icons.Default.Add),
    BottomNavItem(Screen.Chat, "Chat", Icons.Default.Email),
    BottomNavItem(Screen.Profile, "Profile", Icons.Default.Person),
)
