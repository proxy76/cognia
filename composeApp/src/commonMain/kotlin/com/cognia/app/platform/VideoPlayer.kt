package com.cognia.app.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun VideoPlayer(
    url: String,
    isPlaying: Boolean,
    onBuffering: (Boolean) -> Unit,
    modifier: Modifier = Modifier
)
