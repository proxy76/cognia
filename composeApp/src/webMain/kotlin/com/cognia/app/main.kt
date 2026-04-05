package com.cognia.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.cognia.app.ui.create.LocalPlatformFilePicker
import com.cognia.app.ui.create.WebFilePicker
import com.cognia.app.ui.feed.LocalVideoPlayer
import com.cognia.app.ui.feed.WebVideoPlayer
import kotlinx.browser.localStorage
import org.w3c.dom.get
import org.w3c.dom.set

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initAppWeb()
    ComposeViewport {
        CompositionLocalProvider(
            LocalPlatformFilePicker provides WebFilePicker(),
            LocalVideoPlayer provides WebVideoPlayer(),
        ) {
            App()
        }
    }
}

/**
 * Web-specific initialization that injects browser localStorage
 * for session persistence (tokens survive page reloads).
 */
private fun initAppWeb() {
    com.cognia.app.network.ApiClientProvider.init(
        persistGet = { key -> localStorage[key] },
        persistSet = { key, value -> localStorage[key] = value },
        persistRemove = { key -> localStorage.removeItem(key) },
    )
}
