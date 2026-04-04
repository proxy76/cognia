package com.cognia.app

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = run {
    initApp()
    ComposeUIViewController { App() }
}
