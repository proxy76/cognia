package com.cognia.app.ui.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Web-only moderation dashboard placeholder.
 * Displays mock data for pending reviews, reports, and license requests.
 */
@Composable
fun ModerationDashboard() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Reviews", "Reports", "License Requests")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Moderation Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (selectedTab) {
            0 -> PendingReviewsList()
            1 -> ReportsList()
            2 -> LicenseRequestsList()
        }
    }
}

@Composable
private fun PendingReviewsList() {
    val mockReviews = remember {
        listOf(
            MockReview("review-1", "VIDEO", "Introduction to Kotlin", "Alice"),
            MockReview("review-2", "QUIZ", "Kotlin Basics Quiz", "Bob"),
            MockReview("review-3", "VIDEO", "Advanced Coroutines", "Charlie")
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mockReviews) { review ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = review.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${review.contentType} by ${review.creator}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { /* TODO: approve */ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Approve")
                        }
                        OutlinedButton(onClick = { /* TODO: reject */ }) {
                            Text("Reject")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportsList() {
    val mockReports = remember {
        listOf(
            MockReport("report-1", "VIDEO", "Inappropriate content", "PENDING"),
            MockReport("report-2", "QUIZ", "Misleading questions", "PENDING"),
            MockReport("report-3", "VIDEO", "Copyright violation", "REVIEWED")
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mockReports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.reason,
                            style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(report.status) }
                        )
                    }
                    Text(
                        text = "Content type: ${report.contentType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LicenseRequestsList() {
    val mockRequests = remember {
        listOf(
            MockLicenseRequest("req-1", "Alice", "PENDING"),
            MockLicenseRequest("req-2", "Bob", "APPROVED"),
            MockLicenseRequest("req-3", "Charlie", "PENDING")
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(mockRequests) { request ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creator: ${request.creatorName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        AssistChip(
                            onClick = {},
                            label = { Text(request.status) }
                        )
                    }
                    if (request.status == "PENDING") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { /* TODO: approve */ }) {
                                Text("Approve")
                            }
                            OutlinedButton(onClick = { /* TODO: reject */ }) {
                                Text("Reject")
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class MockReview(
    val id: String,
    val contentType: String,
    val title: String,
    val creator: String
)

private data class MockReport(
    val id: String,
    val contentType: String,
    val reason: String,
    val status: String
)

private data class MockLicenseRequest(
    val id: String,
    val creatorName: String,
    val status: String
)
