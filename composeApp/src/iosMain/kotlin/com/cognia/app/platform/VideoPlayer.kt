package com.cognia.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPlayer(
    url: String,
    isPlaying: Boolean,
    onBuffering: (Boolean) -> Unit,
    modifier: Modifier
) {
    val nsUrl = remember(url) { NSURL.URLWithString(url) }
    val player = remember(url) { nsUrl?.let { AVPlayer.playerWithURL(it) } }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            player?.play()
        } else {
            player?.pause()
        }
    }

    DisposableEffect(url) {
        onDispose {
            player?.pause()
        }
    }

    if (player != null) {
        UIKitView(
            modifier = modifier,
            factory = {
                val playerVc = AVPlayerViewController()
                playerVc.player = player
                playerVc.showsPlaybackControls = true
                playerVc.view
            },
            update = { _ ->
                if (isPlaying) player.play() else player.pause()
            }
        )
    }
}
