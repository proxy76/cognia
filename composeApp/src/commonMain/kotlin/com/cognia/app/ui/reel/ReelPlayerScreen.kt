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

@Composable
fun ReelPlayerScreen(
    viewModel: ReelViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {},
    onNavigateToQuiz: (String) -> Unit = {}
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
}

// ── Inline Quiz Page ────────────────────────────────────────────────

@Composable
private fun InlineQuizPage(
    quizPage: QuizPage,
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
