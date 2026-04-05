package com.cognia.app.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cognia.app.ui.theme.BackgroundDark
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonPurpleGlow
import com.cognia.app.ui.theme.NeonViolet
import com.cognia.app.ui.theme.SurfaceDarkCard
import kotlinx.coroutines.delay

@Composable
fun FeedScreen(
    topicFilter: String? = null,
    onNavigateToCreator: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToTopic: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
) {
    val feedViewModel: FeedViewModel = viewModel { FeedViewModel() }

    LaunchedEffect(topicFilter) {
        feedViewModel.loadFeed(topicFilter)
    }

    val state by feedViewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple, strokeWidth = 3.dp)
            }
        }
        state.items.isEmpty() -> {
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
                        text = if (topicFilter != null) "No videos for #$topicFilter" else "No videos yet",
                        color = NeonPurple.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        else -> {
            ImmersiveFeed(
                state = state,
                topicFilter = topicFilter,
                onPageChanged = { page -> feedViewModel.setCurrentIndex(page) },
                onNavigateToCreator = onNavigateToCreator,
                onNavigateToQuiz = onNavigateToQuiz,
                onNavigateToTopic = onNavigateToTopic,
                onNavigateBack = onNavigateBack,
            )
        }
    }
}

// ── Full-screen Immersive Feed (VerticalPager) ────────────────────────

@Composable
private fun ImmersiveFeed(
    state: FeedUiState,
    topicFilter: String?,
    onPageChanged: (Int) -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTopic: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.items.size },
    )

    // Sync pager page changes back to the ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onPageChanged(page)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1,
        ) { pageIndex ->
            val item = state.items[pageIndex]
            val hashtags = deriveHashtags(item)
            var isPlaying by remember { mutableStateOf(true) }

            // Staggered entrance animations — reset per page settle
            var overlayVisible by remember { mutableStateOf(false) }
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage == pageIndex) {
                    overlayVisible = false
                    delay(150)
                    overlayVisible = true
                } else {
                    overlayVisible = false
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures { isPlaying = !isPlaying }
                    }
            ) {
                // ── Video background ──
                VideoBackground(item, isPlaying = isPlaying)

                // ── Top scrim ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.55f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // ── Bottom scrim ──
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Black.copy(alpha = 0.75f),
                                )
                            )
                        )
                )

                // ── Back button (only on topic-filtered pages) ──
                if (topicFilter != null) {
                    AnimatedVisibility(
                        visible = overlayVisible,
                        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it },
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 12.dp, top = 14.dp)
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                // ── Topic badge (when viewing filtered feed) ──
                if (topicFilter != null) {
                    AnimatedVisibility(
                        visible = overlayVisible,
                        enter = fadeIn(tween(400, delayMillis = 50))
                            + slideInVertically(tween(400, delayMillis = 50)) { -it },
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 18.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = NeonPurple.copy(alpha = 0.18f),
                            modifier = Modifier.border(
                                width = 1.dp,
                                color = NeonPurpleBright.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(20.dp)
                            )
                        ) {
                            Text(
                                text = "#$topicFilter",
                                color = NeonPurpleBright,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 7.dp)
                            )
                        }
                    }
                }

                // ── Creator info overlay (top-left, small) ──
                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(tween(450, delayMillis = 120))
                        + slideInHorizontally(tween(450, delayMillis = 120)) { -it / 2 },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(
                            start = 16.dp,
                            top = if (topicFilter != null) 62.dp else 18.dp
                        )
                ) {
                    CreatorInfoOverlay(
                        creatorName = item.creatorName,
                        shortDescription = shortDescription(item.title),
                        onClick = { onNavigateToCreator(item.creatorId) }
                    )
                }

                // ── Right-side action buttons ──
                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(tween(400, delayMillis = 200))
                        + slideInHorizontally(tween(400, delayMillis = 200)) { it / 2 },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 14.dp)
                ) {
                    ActionButtonColumn(
                        hasQuiz = item.hasQuiz,
                        onQuizClick = { onNavigateToQuiz(item.id) }
                    )
                }

                // ── Bottom: Diamond hashtag widget ──
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp)
                ) {
                    AnimatedVisibility(
                        visible = overlayVisible,
                        enter = fadeIn(tween(400, delayMillis = 300))
                            + scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ),
                    ) {
                        HashtagDiamondWidget(
                            hashtags = hashtags,
                            onHashtagClick = onNavigateToTopic,
                        )
                    }
                }

                // ── Play/Pause indicator ──
                AnimatedVisibility(
                    visible = !isPlaying,
                    enter = fadeIn(tween(150)) + scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ),
                    exit = fadeOut(tween(200)) + scaleOut(tween(200)),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.45f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Paused",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }

                // ── Scroll indicator (bouncing arrow at bottom) ──
                if (pageIndex < state.items.size - 1 && isPlaying) {
                    ScrollIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }

                // ── Page counter (subtle, top-right) ──
                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(tween(300, delayMillis = 150)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp, end = 16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Black.copy(alpha = 0.35f),
                    ) {
                        Text(
                            text = "${pageIndex + 1} / ${state.items.size}",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Video Background ────────────────────────────────────────────────

@Composable
private fun VideoBackground(item: FeedItemUi, isPlaying: Boolean = true) {
    val fullVideoUrl = if (item.videoUrl != null) {
        com.cognia.app.network.ApiConfig.baseUrl + item.videoUrl
    } else {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (fullVideoUrl != null) Color.Transparent else Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (fullVideoUrl != null) {
            val videoPlayer = LocalVideoPlayer.current
            videoPlayer.VideoPlayer(
                videoUrl = fullVideoUrl,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            // Placeholder gradient for videos without a stream URL
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF080812),
                                NeonViolet.copy(alpha = 0.07f),
                                NeonPurpleDark.copy(alpha = 0.04f),
                                Color(0xFF080812),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = NeonPurple.copy(alpha = 0.06f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(48.dp)
                )
            }
        }
    }
}

// ── Creator Info Overlay (top-left, small) ──────────────────────────

@Composable
private fun CreatorInfoOverlay(
    creatorName: String,
    shortDescription: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Text(
            text = "@$creatorName",
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = shortDescription,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f),
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}

// ── Action Button Column (right side, TikTok-style) ─────────────────

@Composable
private fun ActionButtonColumn(
    hasQuiz: Boolean,
    onQuizClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FeedActionButton(
            icon = Icons.Default.Favorite,
            label = "Like",
            tint = Color.White,
        )
        FeedActionButton(
            icon = Icons.Default.Bookmark,
            label = "Save",
            tint = Color.White,
        )
        FeedActionButton(
            icon = Icons.Default.Share,
            label = "Share",
            tint = Color.White,
        )
        if (hasQuiz) {
            FeedActionButton(
                icon = Icons.Default.Psychology,
                label = "Quiz",
                tint = NeonCyan,
                onClick = onQuizClick,
            )
        }
    }
}

@Composable
private fun FeedActionButton(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.3f),
            modifier = Modifier.size(48.dp)
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
            color = Color.White.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
        )
    }
}

// ── Hashtag Diamond Widget (bottom center) ──────────────────────────

@Composable
private fun HashtagDiamondWidget(
    hashtags: List<String>,
    onHashtagClick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    // Animate scale: 1.0 → 1.15 when expanded
    val diamondScale by animateFloatAsState(
        targetValue = if (expanded) 1.15f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "diamondScale"
    )

    // Animate glow intensity
    val glowAlpha by animateFloatAsState(
        targetValue = if (expanded) 0.5f else 0.15f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "glowAlpha"
    )

    // Animate hashtag pill reveal
    val hashtagAlpha by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = tween(350, delayMillis = if (expanded) 100 else 0),
        label = "hashtagAlpha"
    )
    val hashtagOffset by animateDpAsState(
        targetValue = if (expanded) 0.dp else 10.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "hashtagOffset"
    )

    val borderColor by animateColorAsState(
        targetValue = if (expanded) NeonPurpleBright.copy(alpha = 0.6f)
            else NeonPurple.copy(alpha = 0.3f),
        animationSpec = tween(300),
        label = "borderColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // ── Hashtag pills revealed above the diamond ──
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
                        DiamondHashtagPill(tag = tag, onClick = { onHashtagClick(tag) })
                    }
                }
                // Row 2: remaining hashtags
                if (hashtags.size > 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        hashtags.drop(2).take(2).forEach { tag ->
                            DiamondHashtagPill(tag = tag, onClick = { onHashtagClick(tag) })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // ── The diamond itself ──
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .scale(diamondScale)
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
private fun DiamondHashtagPill(
    tag: String,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val glowAlpha by animateFloatAsState(
        targetValue = if (isHovered) 0.45f else 0.0f,
        animationSpec = tween(250, easing = FastOutSlowInEasing),
        label = "pillGlow"
    )
    val pillScale by animateFloatAsState(
        targetValue = if (isHovered) 1.06f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "pillScale"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Black.copy(alpha = 0.55f),
        modifier = Modifier
            .scale(pillScale)
            .border(
                width = if (glowAlpha > 0.01f) 1.dp else 0.5.dp,
                color = if (glowAlpha > 0.01f) NeonPurpleGlow.copy(alpha = glowAlpha)
                    else NeonPurple.copy(alpha = 0.25f),
                shape = RoundedCornerShape(20.dp)
            )
            .hoverable(interactionSource)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
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

// ── Scroll Indicator (bouncing arrow) ───────────────────────────────

@Composable
private fun ScrollIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "scrollBounce")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceY"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "bounceAlpha"
    )

    Icon(
        imageVector = Icons.Default.KeyboardArrowDown,
        contentDescription = "Scroll for more",
        tint = Color.White.copy(alpha = alpha),
        modifier = modifier
            .size(28.dp)
            .offset { IntOffset(0, offsetY.toInt()) }
    )
}

// ── Helpers ─────────────────────────────────────────────────────────

/** Truncate a title to at most 6 words for the short description overlay. */
private fun shortDescription(title: String): String {
    val words = title.split(" ").take(6)
    return words.joinToString(" ") + if (title.split(" ").size > 6) "…" else ""
}
