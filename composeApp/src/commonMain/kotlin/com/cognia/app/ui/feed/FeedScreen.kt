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

@Composable
fun FeedScreen(
    onNavigateToVideo: (String) -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {}
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
            )
        }

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
