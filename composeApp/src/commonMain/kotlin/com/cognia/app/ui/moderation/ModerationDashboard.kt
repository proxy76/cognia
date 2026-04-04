package com.cognia.app.ui.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cognia.app.dto.moderation.LicenseRequestResponse
import com.cognia.app.dto.moderation.ModerationQueueItem
import com.cognia.app.dto.moderation.ReportResponse

/**
 * Moderation dashboard wired to real backend APIs.
 * Displays pending reviews, reports, and license requests.
 */
@Composable
fun ModerationDashboard(viewModel: ModerationViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Reviews", "Reports", "License Requests")

    val error by viewModel.error.collectAsState()

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> viewModel.loadQueue()
            1 -> viewModel.loadReports()
            2 -> viewModel.loadLicenseRequests()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Moderation Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            }
        }

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
            0 -> PendingReviewsList(viewModel)
            1 -> ReportsList(viewModel)
            2 -> LicenseRequestsList(viewModel)
        }
    }
}

@Composable
private fun PendingReviewsList(viewModel: ModerationViewModel) {
    val items by viewModel.queueItems.collectAsState()
    val loading by viewModel.queueLoading.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
        return
    }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No pending reviews", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items, key = { it.reviewId }) { review ->
            ReviewCard(review = review, viewModel = viewModel)
        }
    }
}

@Composable
private fun ReviewCard(review: ModerationQueueItem, viewModel: ModerationViewModel) {
    var showRejectDialog by remember { mutableStateOf(false) }

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
                text = "${review.contentType} by ${review.creator.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (review.isPostPublication) {
                Text(
                    text = "Post-publication review",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { viewModel.approveReview(review.reviewId) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Approve")
                }
                OutlinedButton(onClick = { showRejectDialog = true }) {
                    Text("Reject")
                }
            }
        }
    }

    if (showRejectDialog) {
        RejectDialog(
            onDismiss = { showRejectDialog = false },
            onConfirm = { reason, issueStrike ->
                viewModel.rejectReview(review.reviewId, reason, issueStrike)
                showRejectDialog = false
            }
        )
    }
}

@Composable
private fun RejectDialog(onDismiss: () -> Unit, onConfirm: (String?, Boolean) -> Unit) {
    var reason by remember { mutableStateOf("") }
    var issueStrike by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject Content") },
        text = {
            Column {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = issueStrike, onCheckedChange = { issueStrike = it })
                    Text("Issue strike to creator")
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(reason.ifBlank { null }, issueStrike) }) {
                Text("Reject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReportsList(viewModel: ModerationViewModel) {
    val reports by viewModel.reports.collectAsState()
    val loading by viewModel.reportsLoading.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
        return
    }

    if (reports.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No reports", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(reports, key = { it.id }) { report ->
            ReportCard(report = report, viewModel = viewModel)
        }
    }
}

@Composable
private fun ReportCard(report: ReportResponse, viewModel: ModerationViewModel) {
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
            if (report.status == "PENDING") {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.resolveReport(report.id, "REVIEWED") }) {
                        Text("Mark Reviewed")
                    }
                    OutlinedButton(onClick = { viewModel.resolveReport(report.id, "DISMISSED") }) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseRequestsList(viewModel: ModerationViewModel) {
    val requests by viewModel.licenseRequests.collectAsState()
    val loading by viewModel.licenseLoading.collectAsState()

    if (loading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        }
        return
    }

    if (requests.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("No license requests", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(requests, key = { it.id }) { request ->
            LicenseRequestCard(request = request, viewModel = viewModel)
        }
    }
}

@Composable
private fun LicenseRequestCard(request: LicenseRequestResponse, viewModel: ModerationViewModel) {
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
                    text = "Creator: ${request.creatorId}",
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
                    Button(onClick = { viewModel.approveLicense(request.id) }) {
                        Text("Approve")
                    }
                    OutlinedButton(onClick = { viewModel.rejectLicense(request.id) }) {
                        Text("Reject")
                    }
                }
            }
            if (request.rejectionReason != null) {
                Text(
                    text = "Reason: ${request.rejectionReason}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
