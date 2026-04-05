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
import org.w3c.dom.events.Event

/**
 * Web implementation: places an HTML5 <video> element on top of the canvas.
 * pointer-events:none lets clicks pass through to the compose canvas underneath.
 */
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
                setAttribute("playsinline", "true")
                style.cssText = buildString {
                    append("position:fixed;")
                    append("top:0;left:0;")
                    append("width:100vw;height:100vh;")
                    append("object-fit:cover;")
                    append("z-index:10;")              // Above the compose canvas
                    append("pointer-events:none;")     // Clicks pass through to compose
                    append("background:black;")
                }

                // Debug event listeners
                addEventListener("loadeddata", { _: Event ->
                    println("VIDEO: loadeddata — video loaded OK: $videoUrl")
                })
                addEventListener("error", { _: Event ->
                    val errCode = this.error?.code ?: 0
                    println("VIDEO ERROR: code=$errCode for $videoUrl")
                })
                addEventListener("canplay", { _: Event ->
                    println("VIDEO: canplay — ready to play")
                })
                addEventListener("playing", { _: Event ->
                    println("VIDEO: playing now")
                })
                addEventListener("stalled", { _: Event ->
                    println("VIDEO: stalled — data transfer interrupted")
                })
            }
        }

        DisposableEffect(videoUrl) {
            val body = document.body ?: return@DisposableEffect onDispose {}
            body.appendChild(videoElement)
            println("VIDEO: element appended to body, src=$videoUrl")

            onDispose {
                println("VIDEO: disposing element")
                videoElement.pause()
                videoElement.removeAttribute("src")
                videoElement.load()
                try { videoElement.parentElement?.removeChild(videoElement) } catch (_: Exception) {}
            }
        }

        // Play / pause control
        LaunchedEffect(isPlaying) {
            try {
                if (isPlaying) {
                    videoElement.play()
                    println("VIDEO: play() called")
                } else {
                    videoElement.pause()
                    println("VIDEO: pause() called")
                }
            } catch (e: Exception) {
                println("VIDEO: play/pause exception: ${e.message}")
            }
        }

        // Transparent placeholder so compose layout fills the space
        Box(modifier = modifier.fillMaxSize().background(Color.Transparent))
    }
}
