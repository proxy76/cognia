package com.cognia.app.ui.reel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ReelPlayerScreen(
    viewModel: ReelViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    if (state.videos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Text("No videos available", color = Color.White)
        }
        return
    }

    val currentVideo = state.videos[state.currentIndex]
    var showPlayIcon by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) {
                        viewModel.swipeToNext()
                    } else if (dragAmount > 50) {
                        viewModel.swipeToPrevious()
                    }
                }
            }
            .clickable {
                viewModel.togglePlayPause()
                showPlayIcon = true
            }
    ) {
        // Video placeholder with gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A1A2E),
                            Color(0xFF16213E),
                            Color(0xFF0F3460)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            // Video title placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = currentVideo.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Video Player Placeholder",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.2f)
                )
            }
        }

        // Back button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Page indicator
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Text(
                text = "${state.currentIndex + 1}/${state.videos.size}",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Play/Pause icon overlay
        if (!state.isPlaying) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Paused",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.Center)
            )
        }

        // Buffering indicator
        if (state.isBuffering) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Right-side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentVideo.hasQuiz) {
                ActionButton(
                    icon = { Icon(Icons.Default.Psychology, contentDescription = "Quiz", tint = Color.White, modifier = Modifier.size(28.dp)) },
                    label = "Quiz",
                    onClick = { onNavigateToQuiz(currentVideo.id) }
                )
            }
            ActionButton(
                icon = { Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(28.dp)) },
                label = "Share",
                onClick = { /* TODO: Share functionality */ }
            )
        }

        // Bottom overlay with video info
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                // Creator name
                Text(
                    text = "@${currentVideo.creatorName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        onNavigateToCreator(currentVideo.creatorId)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Video title
                Text(
                    text = currentVideo.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
<<<<<<< Updated upstream
private fun ActionButton(
    icon: @Composable () -> Unit,
=======
private fun CreatorOverlay(
    creatorName: String,
    onCreatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Black.copy(alpha = 0.4f),
        modifier = modifier
            .clickable(onClick = onCreatorClick)
            .widthIn(max = 180.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = "@$creatorName",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "Creator",
                style = MaterialTheme.typography.labelSmall,
                color = NeonPurple.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
            )
        }
    }
}

// ── Video Content (shared between mobile & desktop) ─────────────────

@Composable
private fun VideoContent(
    state: ReelUiState,
    currentVideo: VideoItem,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
    showCreatorOverlay: Boolean,
    showBackButton: Boolean,
    showPageIndicator: Boolean,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) onSwipeNext()
                    else if (dragAmount > 50) onSwipePrevious()
                }
            }
            .pointerInput(Unit) {
                detectTapGestures { onTogglePlayPause() }
            }
    ) {
        // Video player or placeholder
        val videoUrl = currentVideo.videoUrl
        if (videoUrl != null) {
            com.cognia.app.platform.VideoPlayer(
                url = videoUrl,
                isPlaying = state.isPlaying,
                onBuffering = { /* handled by viewmodel */ },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Fallback gradient when no video URL is available
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF080812),
                                NeonViolet.copy(alpha = 0.08f),
                                NeonPurpleDark.copy(alpha = 0.05f),
                                Color(0xFF080812),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentVideo.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = NeonPurple.copy(alpha = 0.08f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // Top scrim for readability
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VideoScrimTop,
                            Color.Transparent,
                        )
                    )
                )
        )

        // Play/Pause indicator (only show when no native player)
        if (!state.isPlaying && currentVideo.videoUrl == null) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier
                    .size(68.dp)
                    .align(Alignment.Center),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Paused",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
        }

        // Buffering
        if (state.isBuffering) {
            CircularProgressIndicator(
                color = NeonPurple,
                strokeWidth = 3.dp,
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

// ── Mobile Action Button ────────────────────────────────────────────

@Composable
private fun MobileActionButton(
    icon: ImageVector,
>>>>>>> Stashed changes
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}
