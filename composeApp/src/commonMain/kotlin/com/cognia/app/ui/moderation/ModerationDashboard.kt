package com.cognia.app.ui.moderation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cognia.app.dto.moderation.LicenseRequestResponse
import com.cognia.app.dto.moderation.ModerationQueueItem
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.SurfaceDarkCard

/**
 * Admin/Moderator dashboard for reviewing pending content and license requests.
 * Fetches real data from backend APIs via ModerationViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModerationDashboard(viewModel: ModerationViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pending Reviews", "License Requests")

    // Show success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(state.actionSuccess) {
        state.actionSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearActionSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // Error banner
            if (state.error != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            state.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            Box(modifier = Modifier.padding(16.dp)) {
                when {
                    state.isLoading && selectedTab == 0 -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NeonPurple, strokeWidth = 3.dp)
                        }
                    }
                    selectedTab == 0 -> PendingReviewsList(
                        items = state.queueItems,
                        actionInProgress = state.actionInProgress,
                        onApprove = { viewModel.approveReview(it) },
                        onReject = { id, reason -> viewModel.rejectReview(id, reason) },
                        onRefresh = { viewModel.loadQueue() }
                    )
                    selectedTab == 1 -> LicenseRequestsList(
                        requests = state.licenseRequests,
                        actionInProgress = state.actionInProgress,
                        onApprove = { viewModel.approveLicense(it) },
                        onReject = { id, reason -> viewModel.rejectLicense(id, reason) },
                        onRefresh = { viewModel.loadLicenseRequests() }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingReviewsList(
    items: List<ModerationQueueItem>,
    actionInProgress: String?,
    onApprove: (String) -> Unit,
    onReject: (String, String?) -> Unit,
    onRefresh: () -> Unit
) {
    if (items.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = NeonCyan.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "No pending reviews",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRefresh) {
                Text("Refresh", color = NeonPurpleBright)
            }
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(items, key = { it.reviewId }) { review ->
            var showRejectDialog by remember { mutableStateOf(false) }
            val isActioning = actionInProgress == review.reviewId

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
                        text = "${review.contentType} by ${review.creator.displayName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "Submitted: ${review.submittedAt.take(16).replace("T", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onApprove(review.reviewId) },
                            enabled = !isActioning,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            if (isActioning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Approve", color = MaterialTheme.colorScheme.onPrimary)
                            }
                        }
                        OutlinedButton(
                            onClick = { showRejectDialog = true },
                            enabled = !isActioning,
                            border = ButtonDefaults.outlinedButtonBorder(enabled = true),
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text("Reject", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            if (showRejectDialog) {
                RejectDialog(
                    title = "Reject \"${review.title}\"?",
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        showRejectDialog = false
                        onReject(review.reviewId, reason)
                    }
                )
            }
        }
    }
}

@Composable
private fun LicenseRequestsList(
    requests: List<LicenseRequestResponse>,
    actionInProgress: String?,
    onApprove: (String) -> Unit,
    onReject: (String, String?) -> Unit,
    onRefresh: () -> Unit
) {
    if (requests.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "No license requests",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onRefresh) {
                Text("Refresh", color = NeonPurpleBright)
            }
        }
        return
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(requests, key = { it.id }) { request ->
            var showRejectDialog by remember { mutableStateOf(false) }
            val isActioning = actionInProgress == request.id
            val isPending = request.status == "PENDING"

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
                            text = "Creator: ${request.creatorId.take(12)}…",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (request.status) {
                                "APPROVED" -> NeonCyan.copy(alpha = 0.15f)
                                "REJECTED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                                else -> NeonPurple.copy(alpha = 0.15f)
                            },
                        ) {
                            Text(
                                text = request.status,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = when (request.status) {
                                    "APPROVED" -> NeonCyan
                                    "REJECTED" -> MaterialTheme.colorScheme.error
                                    else -> NeonPurpleBright
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                    Text(
                        text = "Requested: ${request.createdAt.take(16).replace("T", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                    if (isPending) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onApprove(request.id) },
                                enabled = !isActioning,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                shape = MaterialTheme.shapes.small,
                            ) {
                                if (isActioning) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text("Approve", color = MaterialTheme.colorScheme.onPrimary)
                                }
                            }
                            OutlinedButton(
                                onClick = { showRejectDialog = true },
                                enabled = !isActioning,
                                shape = MaterialTheme.shapes.small,
                            ) {
                                Text("Reject", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            if (showRejectDialog) {
                RejectDialog(
                    title = "Reject license request?",
                    onDismiss = { showRejectDialog = false },
                    onConfirm = { reason ->
                        showRejectDialog = false
                        onReject(request.id, reason)
                    }
                )
            }
        }
    }
}

@Composable
private fun RejectDialog(
    title: String,
    onDismiss: () -> Unit,
    onConfirm: (String?) -> Unit
) {
    var reason by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason (optional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(reason.ifBlank { null }) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
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
