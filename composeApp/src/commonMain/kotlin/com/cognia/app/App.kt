package com.cognia.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.cognia.app.navigation.AppNavigation

@Composable
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}
