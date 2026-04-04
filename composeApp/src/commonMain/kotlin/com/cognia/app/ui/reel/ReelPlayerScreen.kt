package com.cognia.app.ui.reel

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tag
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cognia.app.ui.theme.BackgroundDark
import com.cognia.app.ui.theme.LocalWindowWidthClass
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonPurpleGlow
import com.cognia.app.ui.theme.NeonViolet
import com.cognia.app.ui.theme.SurfaceDark
import com.cognia.app.ui.theme.SurfaceDarkCard
import com.cognia.app.ui.theme.VideoScrimBottom
import com.cognia.app.ui.theme.VideoScrimTop
import com.cognia.app.ui.theme.WindowWidthClass

@Composable
fun ReelPlayerScreen(
    viewModel: ReelViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val windowWidthClass = LocalWindowWidthClass.current

    if (state.videos.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().background(BackgroundDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = NeonPurple.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "No videos available",
                    color = NeonPurple.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        return
    }

    val currentVideo = state.videos[state.currentIndex]

    when (windowWidthClass) {
        WindowWidthClass.COMPACT, WindowWidthClass.MEDIUM -> {
            // Mobile / tablet: fullscreen immersive
            ImmersiveVideoPlayer(
                state = state,
                currentVideo = currentVideo,
                onNavigateBack = onNavigateBack,
                onNavigateToCreator = onNavigateToCreator,
                onNavigateToQuiz = onNavigateToQuiz,
                onSwipeNext = viewModel::swipeToNext,
                onSwipePrevious = viewModel::swipeToPrevious,
                onTogglePlayPause = viewModel::togglePlayPause,
            )
        }
        WindowWidthClass.EXPANDED -> {
            // Desktop: centered video with side context
            DesktopVideoLayout(
                state = state,
                currentVideo = currentVideo,
                onNavigateBack = onNavigateBack,
                onNavigateToCreator = onNavigateToCreator,
                onNavigateToQuiz = onNavigateToQuiz,
                onSwipeNext = viewModel::swipeToNext,
                onSwipePrevious = viewModel::swipeToPrevious,
                onTogglePlayPause = viewModel::togglePlayPause,
            )
        }
    }
}

// ── Desktop Layout ──────────────────────────────────────────────────

@Composable
private fun DesktopVideoLayout(
    state: ReelUiState,
    currentVideo: VideoItem,
    onNavigateBack: () -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side panel - navigation & creator info
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .padding(end = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Back + page indicator
                Column {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonPurpleBright
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonPurple.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text = "${state.currentIndex + 1} of ${state.videos.size}",
                            color = NeonPurpleBright.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Creator card
                DesktopCreatorCard(
                    creatorName = currentVideo.creatorName,
                    title = currentVideo.title,
                    onCreatorClick = { onNavigateToCreator(currentVideo.creatorId) }
                )
            }

            // Center - video player (9:16 aspect ratio, bounded)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 400.dp)
                    .aspectRatio(9f / 16f, matchHeightConstraintsFirst = true)
                    .clip(RoundedCornerShape(20.dp))
                    .shadow(24.dp, RoundedCornerShape(20.dp))
            ) {
                VideoContent(
                    state = state,
                    currentVideo = currentVideo,
                    onSwipeNext = onSwipeNext,
                    onSwipePrevious = onSwipePrevious,
                    onTogglePlayPause = onTogglePlayPause,
                    showCreatorOverlay = false, // Creator info is in side panel
                    showBackButton = false,
                    showPageIndicator = false,
                )

                // Diamond widget still overlays the video on desktop
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 28.dp)
                ) {
                    HashtagDiamondWidget(
                        hashtags = videoHashtags(currentVideo),
                    )
                }
            }

            // Right side panel - actions
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .padding(start = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                DesktopActionBar(
                    currentVideo = currentVideo,
                    onNavigateToQuiz = onNavigateToQuiz
                )
            }
        }
    }
}

@Composable
private fun DesktopCreatorCard(
    creatorName: String,
    title: String,
    onCreatorClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = SurfaceDarkCard,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "@$creatorName",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurpleBright,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onCreatorClick)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Content Creator",
                style = MaterialTheme.typography.bodySmall,
                color = NeonPurple.copy(alpha = 0.5f),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DesktopActionBar(
    currentVideo: VideoItem,
    onNavigateToQuiz: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DesktopActionButton(
            icon = Icons.Default.Favorite,
            label = "Like",
            tint = NeonPurple,
        )
        DesktopActionButton(
            icon = Icons.Default.Bookmark,
            label = "Save",
            tint = NeonPurpleBright,
        )
        DesktopActionButton(
            icon = Icons.Default.Share,
            label = "Share",
            tint = Color.White.copy(alpha = 0.7f),
        )
        if (currentVideo.hasQuiz) {
            DesktopActionButton(
                icon = Icons.Default.Psychology,
                label = "Take Quiz",
                tint = NeonCyan,
                onClick = { onNavigateToQuiz(currentVideo.id) }
            )
        }
    }
}

@Composable
private fun DesktopActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDarkCard,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

// ── Mobile Immersive Layout ─────────────────────────────────────────

@Composable
private fun ImmersiveVideoPlayer(
    state: ReelUiState,
    currentVideo: VideoItem,
    onNavigateBack: () -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onSwipeNext: () -> Unit,
    onSwipePrevious: () -> Unit,
    onTogglePlayPause: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        VideoContent(
            state = state,
            currentVideo = currentVideo,
            onSwipeNext = onSwipeNext,
            onSwipePrevious = onSwipePrevious,
            onTogglePlayPause = onTogglePlayPause,
            showCreatorOverlay = true,
            showBackButton = true,
            showPageIndicator = true,
        )

        // Back button (top-left)
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        // Creator info (top-right)
        CreatorOverlay(
            creatorName = currentVideo.creatorName,
            onCreatorClick = { onNavigateToCreator(currentVideo.creatorId) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 16.dp, end = 16.dp)
        )

        // Page indicator (top-center)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.35f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
        ) {
            Text(
                text = "${state.currentIndex + 1} / ${state.videos.size}",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
            )
        }

        // Right-side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MobileActionButton(
                icon = Icons.Default.Favorite,
                label = "Like",
                tint = Color.White,
            )
            MobileActionButton(
                icon = Icons.Default.Bookmark,
                label = "Save",
                tint = Color.White,
            )
            if (currentVideo.hasQuiz) {
                MobileActionButton(
                    icon = Icons.Default.Psychology,
                    label = "Quiz",
                    tint = NeonCyan,
                    onClick = { onNavigateToQuiz(currentVideo.id) }
                )
            }
            MobileActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
            )
        }

        // Bottom: video title + hashtag diamond
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.65f),
                        )
                    )
                )
                .padding(bottom = 24.dp, start = 16.dp, end = 72.dp, top = 48.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // Video title
            Text(
                text = currentVideo.title,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Hashtag diamond widget (bottom-center)
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                HashtagDiamondWidget(
                    hashtags = videoHashtags(currentVideo),
                )
            }
        }
    }
}

// ── Creator Overlay (top-right on mobile) ───────────────────────────

@Composable
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
        // Video placeholder with subtle neon gradient
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
            // Faint title watermark
            Text(
                text = currentVideo.title,
                style = MaterialTheme.typography.headlineMedium,
                color = NeonPurple.copy(alpha = 0.08f),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
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

        // Play/Pause indicator
        if (!state.isPlaying) {
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
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.size(46.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tint,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
        )
    }
}

// ── Hashtag Diamond Widget ──────────────────────────────────────────

@Composable
private fun HashtagDiamondWidget(
    hashtags: List<String>,
) {
    var expanded by remember { mutableStateOf(false) }

    // Animate scale: 1.0 → 1.15 when expanded
    val scale by animateFloatAsState(
        targetValue = if (expanded) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "diamondScale"
    )

    // Animate glow
    val glowAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.5f else 0.15f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "glowAlpha"
    )

    // Animate hashtag reveal
    val hashtagAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(350, delayMillis = if (expanded) 100 else 0),
        label = "hashtagAlpha"
    )

    val hashtagOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "hashtagOffset"
    )

    val borderColor by animateColorAsState(
        targetValue = if (expanded) NeonPurpleBright.copy(alpha = 0.6f) else NeonPurple.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Hashtag pills revealed above the diamond
        if (hashtagAlpha > 0.01f) {
            Column(
                modifier = Modifier
                    .alpha(hashtagAlpha)
                    .offset(y = hashtagOffset),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                // Row 1: first 2 hashtags
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    hashtags.take(2).forEach { tag ->
                        HashtagPill(tag)
                    }
                }
                // Row 2: last 2 hashtags
                if (hashtags.size > 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        hashtags.drop(2).take(2).forEach { tag ->
                            HashtagPill(tag)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // The diamond itself
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = !expanded }
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .rotate(45f)
                    .background(
                        NeonPurpleGlow.copy(alpha = glowAlpha),
                        RoundedCornerShape(10.dp)
                    )
            )

            // Diamond body
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .rotate(45f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonPurpleDark.copy(alpha = 0.85f),
                                NeonViolet.copy(alpha = 0.7f),
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = borderColor,
                        shape = RoundedCornerShape(9.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Tag,
                    contentDescription = "Topics",
                    tint = NeonPurpleBright,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(-45f) // Counter-rotate to keep icon upright
                )
            }
        }
    }
}

@Composable
private fun HashtagPill(tag: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.55f),
        modifier = Modifier
            .border(
                width = 0.5.dp,
                color = NeonPurple.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelSmall,
            color = NeonPurpleBright.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────

private fun videoHashtags(video: VideoItem): List<String> {
    // Derive hashtags from video title words as preview data
    val words = video.title
        .split(" ", "-", ":", ",")
        .filter { it.length > 3 }
        .map { it.lowercase().trim() }
        .distinct()
        .take(4)
    return if (words.size >= 2) words else listOf("education", "learning", "cognia", "knowledge")
}
