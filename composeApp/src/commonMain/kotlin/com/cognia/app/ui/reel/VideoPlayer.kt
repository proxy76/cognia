package com.cognia.app.ui.reel

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific video player composable.
 * On iOS uses AVPlayer, on other platforms shows a placeholder.
 */
expect @Composable fun NativeVideoPlayer(
    url: String,
    isPlaying: Boolean,
    modifier: Modifier,
)
