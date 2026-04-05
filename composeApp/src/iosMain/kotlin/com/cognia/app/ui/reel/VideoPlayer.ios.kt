package com.cognia.app.ui.reel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.Foundation.NSURL
import platform.UIKit.UIView
import platform.QuartzCore.CALayer

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeVideoPlayer(
    url: String,
    isPlaying: Boolean,
    modifier: Modifier,
) {
    val player = remember { AVPlayer() }
    val playerLayer = remember { AVPlayerLayer() }

    LaunchedEffect(url) {
        val nsUrl = NSURL.URLWithString(url) ?: return@LaunchedEffect
        val item = AVPlayerItem(uRL = nsUrl)
        player.replaceCurrentItemWithPlayerItem(item)
        if (isPlaying) player.play()
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) player.play() else player.pause()
    }

    DisposableEffect(Unit) {
        onDispose {
            player.pause()
        }
    }

    UIKitView(
        modifier = modifier,
        factory = {
            val container = UIView()
            playerLayer.player = player
            playerLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
            container.layer.addSublayer(playerLayer)
            container
        },
        update = { view ->
            playerLayer.frame = view.bounds
        },
    )
}
