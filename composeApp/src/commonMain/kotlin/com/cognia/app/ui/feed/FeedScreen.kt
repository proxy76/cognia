package com.cognia.app.ui.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
<<<<<<< Updated upstream
=======
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
>>>>>>> Stashed changes
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cognia.app.ui.theme.LocalWindowWidthClass
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonViolet
import com.cognia.app.ui.theme.SurfaceDarkCard
import com.cognia.app.ui.theme.SurfaceDarkElevated
import com.cognia.app.ui.theme.WindowWidthClass

/** Accent color for the ELI5 (Explain Like I'm Five) button. */
private val Eli5Amber = Color(0xFFFFB74D)

@Composable
fun FeedScreen(
<<<<<<< Updated upstream
    onNavigateToVideo: (String) -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {}
=======
    topicFilter: String? = null,
    onNavigateToCreator: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToTopic: (String) -> Unit = {},
    onNavigateToEli5: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
>>>>>>> Stashed changes
) {
    val feedViewModel: FeedViewModel = viewModel { FeedViewModel() }
    val state by feedViewModel.state.collectAsState()
    val windowWidthClass = LocalWindowWidthClass.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab bar
        TabRow(
            selectedTabIndex = if (state.selectedTab == FeedTab.FOR_YOU) 0 else 1,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = NeonPurple,
            divider = {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                    thickness = 0.5.dp,
                )
            },
        ) {
            Tab(
                selected = state.selectedTab == FeedTab.FOR_YOU,
                onClick = { feedViewModel.selectTab(FeedTab.FOR_YOU) },
                text = {
                    Text(
                        "For You",
                        fontWeight = if (state.selectedTab == FeedTab.FOR_YOU) FontWeight.Bold else FontWeight.Normal,
                    )
<<<<<<< Updated upstream
                },
                selectedContentColor = NeonPurpleBright,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Tab(
                selected = state.selectedTab == FeedTab.DEEP_DIVE,
                onClick = { feedViewModel.selectTab(FeedTab.DEEP_DIVE) },
                text = {
                    Text(
                        "Deep Dive",
                        fontWeight = if (state.selectedTab == FeedTab.DEEP_DIVE) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                selectedContentColor = NeonPurpleBright,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
=======
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
                onNavigateToEli5 = onNavigateToEli5,
                onNavigateBack = onNavigateBack,
>>>>>>> Stashed changes
            )
        }

<<<<<<< Updated upstream
        // Content
        when {
            state.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 3.dp,
                    )
                }
            }
            state.items.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = NeonPurple.copy(alpha = 0.25f),
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No videos yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pull down to refresh",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
            else -> {
                // Use grid on wider screens for a richer desktop experience
                val useGrid = windowWidthClass == WindowWidthClass.EXPANDED
                if (useGrid) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
=======
// ── Full-screen Immersive Feed (VerticalPager) ────────────────────────

@Composable
private fun ImmersiveFeed(
    state: FeedUiState,
    topicFilter: String?,
    onPageChanged: (Int) -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTopic: (String) -> Unit,
    onNavigateToEli5: (String) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.pages.size },
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
            when (val page = state.pages[pageIndex]) {
                is FeedPageItem.Video -> {
                    val item = page.item
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
                                onQuizClick = { onNavigateToQuiz(item.id) },
                                eli5VideoId = item.eli5VideoId,
                                onEli5Click = { eli5Id -> onNavigateToEli5(eli5Id) }
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
                        if (pageIndex < state.pages.size - 1 && isPlaying) {
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
                                    text = "${pageIndex + 1} / ${state.pages.size}",
                                    color = Color.White.copy(alpha = 0.55f),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
                is FeedPageItem.QuizCard -> {
                    InlineQuizPage(
                        videoId = page.videoId,
                        creatorName = page.creatorName,
                        categoryName = page.categoryName,
                        onStartQuiz = { onNavigateToQuiz(page.videoId) }
                    )
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
    eli5VideoId: String? = null,
    onEli5Click: (String) -> Unit = {},
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
        if (eli5VideoId != null) {
            FeedActionButton(
                icon = Icons.Default.ChildCare,
                label = "ELI5",
                tint = Eli5Amber,
                onClick = { onEli5Click(eli5VideoId) },
            )
        }
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
>>>>>>> Stashed changes
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            FeedCard(
                                item = item,
                                onClick = { onNavigateToVideo(item.id) },
                                onCreatorClick = { onNavigateToCreator(item.creatorId) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            FeedCard(
                                item = item,
                                onClick = { onNavigateToVideo(item.id) },
                                onCreatorClick = { onNavigateToCreator(item.creatorId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedCard(
    item: FeedItemUi,
    onClick: () -> Unit,
    onCreatorClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            // Thumbnail placeholder with gradient + play icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonViolet.copy(alpha = 0.2f),
                                NeonPurpleDark.copy(alpha = 0.12f),
                                SurfaceDarkElevated,
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Centered play button indicator
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Black.copy(alpha = 0.35f),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // Title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Creator name
                Text(
                    text = "@${item.creatorName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonPurple.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable(onClick = onCreatorClick),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Chips row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CogniaChip(text = item.categoryName)
                    if (item.difficulty != null) {
                        CogniaChip(text = item.difficulty)
                    }
                    if (item.hasQuiz) {
                        CogniaChip(text = "Quiz", accent = true)
                    }
                    if (item.hasEli5) {
                        CogniaChip(text = "ELI5", accent = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun CogniaChip(text: String, accent: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (accent) NeonCyan.copy(alpha = 0.12f) else NeonPurple.copy(alpha = 0.08f),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (accent) NeonCyan else NeonPurpleBright.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}
<<<<<<< Updated upstream
=======

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

// ── Inline Quiz Page (shown between videos in pager) ───────────────

@Composable
private fun InlineQuizPage(
    videoId: String,
    creatorName: String,
    categoryName: String,
    onStartQuiz: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080812),
                        NeonCyan.copy(alpha = 0.06f),
                        NeonPurpleDark.copy(alpha = 0.04f),
                        Color(0xFF080812),
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Quiz icon
            Surface(
                shape = CircleShape,
                color = NeonCyan.copy(alpha = 0.15f),
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Text(
                text = "Quiz Time!",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )

            Text(
                text = "Test your knowledge on $categoryName",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )

            Text(
                text = "by @$creatorName",
                style = MaterialTheme.typography.bodyMedium,
                color = NeonPurpleBright.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                onClick = onStartQuiz,
                shape = RoundedCornerShape(28.dp),
                color = NeonCyan,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Start Quiz",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                    )
                }
            }

            Text(
                text = "3 questions \u2022 3 options each",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.4f),
            )
        }
    }
}

// ── Helpers ─────────────────────────────────────────────────────────

/** Truncate a title to at most 6 words for the short description overlay. */
private fun shortDescription(title: String): String {
    val words = title.split(" ").take(6)
    return words.joinToString(" ") + if (title.split(" ").size > 6) "…" else ""
}
>>>>>>> Stashed changes
