package com.cognia.app.ui.reel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
<<<<<<< Updated upstream
=======
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
>>>>>>> Stashed changes
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cognia.app.ui.quiz.QuizTakingScreen
import com.cognia.app.ui.quiz.QuizTakingViewModel
import com.cognia.app.ui.theme.BackgroundDark
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright

/** Accent color for the ELI5 (Explain Like I'm Five) button. */
private val Eli5Amber = Color(0xFFFFB74D)

@Composable
fun ReelPlayerScreen(
    viewModel: ReelViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {},
<<<<<<< Updated upstream
    onNavigateToQuiz: (String) -> Unit = {}
=======
    onNavigateToQuiz: (String) -> Unit = {},
    onNavigateToTopic: (String) -> Unit = {},
    onNavigateToEli5: (String) -> Unit = {},
>>>>>>> Stashed changes
) {
    val state by viewModel.state.collectAsState()

    when {
        state.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonPurple)
            }
        }
        state.error != null -> {
            Box(
                modifier = Modifier.fillMaxSize().background(BackgroundDark),
                contentAlignment = Alignment.Center
            ) {
                Text(state.error ?: "Unknown error", color = Color.White)
            }
        }
        state.pages.isEmpty() -> {
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
        }
        else -> {
            val pagerState = rememberPagerState(pageCount = { state.pages.size })

            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
            ) { pageIndex ->
                val page = state.pages[pageIndex]
                val isCurrentPage = pagerState.currentPage == pageIndex

                when (page) {
                    is VideoPage -> {
                        val isEli5 = page.id in state.eli5Videos
                        val currentUrl = if (isEli5 && page.eli5StreamUrl != null) {
                            page.eli5StreamUrl
                        } else {
                            page.streamUrl
                        }

                        VideoReelPage(
                            videoPage = page,
                            currentUrl = currentUrl,
                            isPlaying = isCurrentPage,
                            isEli5 = isEli5,
                            onToggleEli5 = { viewModel.toggleEli5(page.id) },
                            onNavigateToCreator = onNavigateToCreator,
                        )
                    }
                    is QuizPage -> {
                        InlineQuizPage(
                            quizPage = page,
                        )
                    }
                }
            }
        }
    }
}

// ── Video Page (TikTok-style full-screen with overlay) ──────────────

@Composable
private fun VideoReelPage(
    videoPage: VideoPage,
    currentUrl: String,
    isPlaying: Boolean,
    isEli5: Boolean,
    onToggleEli5: () -> Unit,
    onNavigateToCreator: (String) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Native video player fills the entire screen
        NativeVideoPlayer(
            url = currentUrl,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize(),
        )

        // ELI5 mode banner at top
        if (isEli5) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = NeonCyan.copy(alpha = 0.25f),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 54.dp)
            ) {
                Text(
                    text = "Explain Like I'm 5",
                    color = NeonCyan,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

<<<<<<< Updated upstream
        // Right-side action buttons
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ActionButton(
                icon = Icons.Default.Favorite,
                label = "Like",
                tint = Color.White,
            )
            // ELI5 button (below Like)
            if (videoPage.eli5StreamUrl != null) {
                ActionButton(
                    icon = Icons.Default.ChildCare,
                    label = if (isEli5) "Normal" else "ELI5",
                    tint = if (isEli5) NeonCyan else Color.White,
                    onClick = onToggleEli5,
                )
            }
            // Quiz button
            if (videoPage.hasQuiz) {
                ActionButton(
                    icon = Icons.Default.Psychology,
                    label = "Quiz",
                    tint = NeonCyan,
                )
            }
            ActionButton(
                icon = Icons.Default.Share,
                label = "Share",
                tint = Color.White,
            )
        }

        // Bottom: creator name + video title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f),
                        )
                    )
                )
                .padding(bottom = 32.dp, start = 16.dp, end = 72.dp, top = 48.dp),
        ) {
            Text(
                text = "@${videoPage.creatorName}",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onNavigateToCreator(videoPage.creatorId) },
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = videoPage.title,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
=======
    // TikTok-style immersive layout for all screen sizes
    ImmersiveVideoPlayer(
        state = state,
        onNavigateBack = onNavigateBack,
        onNavigateToCreator = onNavigateToCreator,
        onNavigateToQuiz = onNavigateToQuiz,
        onNavigateToTopic = onNavigateToTopic,
        onNavigateToEli5 = onNavigateToEli5,
        onTogglePlayPause = viewModel::togglePlayPause,
    )
>>>>>>> Stashed changes
}

// ── Inline Quiz Page ────────────────────────────────────────────────

@Composable
<<<<<<< Updated upstream
private fun InlineQuizPage(
    quizPage: QuizPage,
=======
private fun ImmersiveVideoPlayer(
    state: ReelUiState,
    onNavigateBack: () -> Unit,
    onNavigateToCreator: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToTopic: (String) -> Unit,
    onNavigateToEli5: (String) -> Unit,
    onTogglePlayPause: () -> Unit,
>>>>>>> Stashed changes
) {
    val quizViewModel: QuizTakingViewModel = viewModel(key = quizPage.quizId) { QuizTakingViewModel() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Quiz: ${quizPage.videoTitle}",
                    style = MaterialTheme.typography.titleMedium,
                    color = NeonPurpleBright,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
<<<<<<< Updated upstream
=======

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

                // ── Back button (top-left) ──
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

                // ── Creator info (top-left, below back button) ──
                AnimatedVisibility(
                    visible = overlayVisible,
                    enter = fadeIn(tween(450, delayMillis = 120))
                        + slideInHorizontally(tween(450, delayMillis = 120)) { -it / 2 },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 58.dp)
                ) {
                    Column(
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { onNavigateToCreator(video.creatorId) }
                    ) {
                        Text(
                            text = "@${video.creatorName}",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shortDesc(video.title),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                    }
                }

                // ── Page indicator (top-right) ──
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
                            text = "${pageIndex + 1} / ${state.videos.size}",
                            color = Color.White.copy(alpha = 0.55f),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ReelActionButton(
                            icon = Icons.Default.Favorite,
                            label = "Like",
                            tint = Color.White,
                        )
                        if (video.eli5VideoId != null) {
                            ReelActionButton(
                                icon = Icons.Default.ChildCare,
                                label = "ELI5",
                                tint = Eli5Amber,
                                onClick = { video.eli5VideoId?.let(onNavigateToEli5) }
                            )
                        }
                        ReelActionButton(
                            icon = Icons.Default.Bookmark,
                            label = "Save",
                            tint = Color.White,
                        )
                        ReelActionButton(
                            icon = Icons.Default.Share,
                            label = "Share",
                            tint = Color.White,
                        )
                        if (video.hasQuiz) {
                            ReelActionButton(
                                icon = Icons.Default.Psychology,
                                label = "Quiz",
                                tint = NeonCyan,
                                onClick = { onNavigateToQuiz(video.id) }
                            )
                        }
                    }
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
                        ReelHashtagDiamondWidget(
                            hashtags = hashtags,
                            onHashtagClick = onNavigateToTopic,
                        )
                    }
                }

                // ── Play/Pause indicator ──
                AnimatedVisibility(
                    visible = !state.isPlaying,
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
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Paused",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    }
                }

                // ── Buffering ──
                if (state.isBuffering) {
                    CircularProgressIndicator(
                        color = NeonPurple,
                        strokeWidth = 3.dp,
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.Center)
                    )
                }

                // ── Scroll indicator ──
                if (pageIndex < state.videos.size - 1 && state.isPlaying) {
                    ReelScrollIndicator(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 4.dp)
                    )
                }
>>>>>>> Stashed changes
            }

            // Quiz content
            QuizTakingScreen(
                viewModel = quizViewModel,
                quizId = quizPage.quizId,
                onDone = { /* User can swipe to next */ }
            )
        }
    }
}

// ── Action Button ───────────────────────────────────────────────────

@Composable
private fun ActionButton(
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
