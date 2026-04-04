package com.cognia.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.cognia.app.navigation.AppNavigation
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.ui.theme.CogniaTheme
import com.cognia.app.ui.theme.LocalWindowWidthClass
import com.cognia.app.ui.theme.windowWidthClassOf

@Composable
fun App() {
    CogniaTheme {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthClass = windowWidthClassOf(maxWidth)
            CompositionLocalProvider(LocalWindowWidthClass provides widthClass) {
                AppNavigation()
            }
        }
    }
}

/**
 * Call this once from each platform's entry point before composing App().
 */
fun initApp() {
    ApiClientProvider.init()
}
