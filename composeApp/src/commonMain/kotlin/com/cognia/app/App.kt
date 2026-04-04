package com.cognia.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.cognia.app.navigation.AppNavigation
import com.cognia.app.network.ApiClientProvider

@Composable
fun App() {
    MaterialTheme {
        AppNavigation()
    }
}

/**
 * Call this once from each platform's entry point before composing App().
 */
fun initApp() {
    ApiClientProvider.init()
}
