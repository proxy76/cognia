package com.cognia.app.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
actual fun VideoPlayer(
    url: String,
    isPlaying: Boolean,
    onBuffering: (Boolean) -> Unit,
    modifier: Modifier
) {
    // Web video player not yet implemented
    Box(
        modifier = modifier.fillMaxSize().background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Video player coming soon", color = Color.White)
    }
}
