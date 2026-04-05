package com.cognia.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.cognia.app.ui.theme.NeonPurple

/**
 * Interface for platform-specific video player implementations.
 * On web: renders an HTML5 <video> element.
 * On other platforms: shows a placeholder.
 */
interface PlatformVideoPlayerFactory {
    @Composable
    fun VideoPlayer(
        videoUrl: String,
        isPlaying: Boolean,
        modifier: Modifier,
    )
}

/**
 * Default no-op video player for platforms that don't have a native implementation.
 */
class NoOpVideoPlayer : PlatformVideoPlayerFactory {
    @Composable
    override fun VideoPlayer(videoUrl: String, isPlaying: Boolean, modifier: Modifier) {
        Box(
            modifier = modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Video playback not supported on this platform",
                color = NeonPurple.copy(alpha = 0.3f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(48.dp)
            )
        }
    }
}

/**
 * Composition local to inject the platform-specific player.
 * Web sets this via CompositionLocalProvider in the App composable.
 */
val LocalVideoPlayer = staticCompositionLocalOf<PlatformVideoPlayerFactory> { NoOpVideoPlayer() }
