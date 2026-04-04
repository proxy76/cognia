package com.cognia.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun FeedScreen(
    onNavigateToVideo: (String) -> Unit = {},
    onNavigateToCreator: (String) -> Unit = {}
) {
    val feedViewModel: FeedViewModel = viewModel { FeedViewModel() }
    val state by feedViewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab bar
        TabRow(
            selectedTabIndex = if (state.selectedTab == FeedTab.FOR_YOU) 0 else 1
        ) {
            Tab(
                selected = state.selectedTab == FeedTab.FOR_YOU,
                onClick = { feedViewModel.selectTab(FeedTab.FOR_YOU) },
                text = { Text("For You") }
            )
            Tab(
                selected = state.selectedTab == FeedTab.DEEP_DIVE,
                onClick = { feedViewModel.selectTab(FeedTab.DEEP_DIVE) },
                text = { Text("Deep Dive") }
            )
        }

        // Content
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No videos yet", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Thumbnail placeholder
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "Video",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Creator name
            Text(
                text = item.creatorName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clickable(onClick = onCreatorClick)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category chip and quiz indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = { Text(item.categoryName, style = MaterialTheme.typography.labelSmall) }
                )
                if (item.difficulty != null) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(item.difficulty, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                if (item.hasQuiz) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Quiz", style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
