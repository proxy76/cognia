package com.cognia.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SearchScreenContent(
    onResultClick: (String, String) -> Unit = { _, _ -> }
) {
    val searchViewModel: SearchViewModel = viewModel { SearchViewModel() }
    val state by searchViewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = state.query,
            onValueChange = { searchViewModel.updateQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Search videos, quizzes, creators...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { searchViewModel.clearQuery() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        // Tabs
        val tabs = SearchTab.entries
        val selectedIndex = tabs.indexOf(state.selectedTab)
        ScrollableTabRow(selectedTabIndex = selectedIndex) {
            tabs.forEach { tab ->
                Tab(
                    selected = state.selectedTab == tab,
                    onClick = { searchViewModel.selectTab(tab) },
                    text = {
                        Text(
                            when (tab) {
                                SearchTab.ALL -> "All"
                                SearchTab.VIDEOS -> "Videos"
                                SearchTab.QUIZZES -> "Quizzes"
                                SearchTab.CREATORS -> "Creators"
                            }
                        )
                    }
                )
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.query.isBlank()) {
            // Show recent searches
            if (state.recentSearches.isNotEmpty()) {
                Text(
                    text = "Recent Searches",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn {
                    items(state.recentSearches) { search ->
                        ListItem(
                            headlineContent = { Text(search) },
                            modifier = Modifier.clickable {
                                searchViewModel.updateQuery(search)
                            }
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Start typing to search", style = MaterialTheme.typography.bodyLarge)
                }
            }
        } else if (state.results.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results found", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(state.results, key = { "${it.type}-${it.id}" }) { result ->
                    ListItem(
                        headlineContent = { Text(result.title) },
                        supportingContent = { Text(result.subtitle) },
                        modifier = Modifier.clickable {
                            onResultClick(result.type, result.id)
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}
