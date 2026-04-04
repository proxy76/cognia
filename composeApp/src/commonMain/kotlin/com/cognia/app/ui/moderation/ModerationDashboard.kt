package com.cognia.app.ui.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.SurfaceDarkCard

/**
 * Web-only moderation dashboard placeholder.
 * Displays mock data for pending reviews, reports, and license requests.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationDashboard() {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Reviews", "Reports", "License Requests")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Moderation Dashboard",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = NeonPurple,
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                },
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                        selectedContentColor = NeonPurpleBright,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                when (selectedTab) {
                    0 -> PendingReviewsList()
                    1 -> ReportsList()
                    2 -> LicenseRequestsList()
                }
            }
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(mockReviews) { review ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = review.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${review.contentType} by ${review.creator}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { /* TODO: approve */ },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text("Approve", color = MaterialTheme.colorScheme.onPrimary)
                        }
                        OutlinedButton(
                            onClick = { /* TODO: reject */ },
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text("Reject", color = MaterialTheme.colorScheme.error)
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(mockReports) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = report.reason,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (report.status == "PENDING") NeonPurple.copy(alpha = 0.15f)
                                    else NeonCyan.copy(alpha = 0.15f),
                        ) {
                            Text(
                                text = report.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (report.status == "PENDING") NeonPurpleBright else NeonCyan,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Content type: ${report.contentType}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(mockRequests) { request ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creator: ${request.creatorName}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (request.status) {
                                "APPROVED" -> NeonCyan.copy(alpha = 0.15f)
                                else -> NeonPurple.copy(alpha = 0.15f)
                            },
                        ) {
                            Text(
                                text = request.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = when (request.status) {
                                    "APPROVED" -> NeonCyan
                                    else -> NeonPurpleBright
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    if (request.status == "PENDING") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { /* TODO: approve */ },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text("Approve", color = MaterialTheme.colorScheme.onPrimary)
                            }
                            OutlinedButton(
                                onClick = { /* TODO: reject */ },
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text("Reject", color = MaterialTheme.colorScheme.error)
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
