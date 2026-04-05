package com.cognia.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.browser.document
import org.w3c.dom.HTMLVideoElement

class WebVideoPlayer : PlatformVideoPlayerFactory {

    @Composable
    override fun VideoPlayer(
        videoUrl: String,
        isPlaying: Boolean,
        modifier: Modifier,
    ) {
        val videoElement = remember(videoUrl) {
            (document.createElement("video") as HTMLVideoElement).apply {
                src = videoUrl
                autoplay = true
                loop = true
                muted = true
                preload = "auto"
                setAttribute("playsinline", "true")
                style.cssText = buildString {
                    append("position:fixed;")
                    append("top:0;left:0;")
                    append("width:100vw;height:100vh;")
                    append("object-fit:cover;")
                    append("z-index:2147483647;")       // Max z-index — above Compose canvas
                    append("pointer-events:none;")
                    append("background:black;")
                }
            }
        }

        DisposableEffect(videoUrl) {
            val body = document.body ?: return@DisposableEffect onDispose {}
            body.appendChild(videoElement)

            onDispose {
                videoElement.pause()
                videoElement.removeAttribute("src")
                videoElement.load()
                try { videoElement.parentElement?.removeChild(videoElement) } catch (_: Exception) {}
            }
        }

        LaunchedEffect(isPlaying) {
            try {
                if (isPlaying) videoElement.play() else videoElement.pause()
            } catch (_: Exception) {}
        }

        Box(modifier = modifier.fillMaxSize().background(Color.Transparent))
    }
}
