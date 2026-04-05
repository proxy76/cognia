package com.cognia.app.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.NeonPurpleDark
import com.cognia.app.ui.theme.NeonViolet
import com.cognia.app.ui.theme.SurfaceDarkCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(viewModel: UploadViewModel) {
    val state by viewModel.state.collectAsState()
    val filePicker = LocalPlatformFilePicker.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Create Video",
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    ) { paddingValues ->
        if (state.uploadSuccess) {
            SuccessContent(
                onCreateAnother = { viewModel.clearState() },
                modifier = Modifier.padding(paddingValues)
            )
        } else {
            UploadFormContent(
                state = state,
                viewModel = viewModel,
                filePicker = filePicker,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun SuccessContent(onCreateAnother: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            modifier = Modifier.size(72.dp),
            tint = NeonCyan,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Video uploaded!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your video has been published and is now in the feed!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateAnother,
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text("Create Another")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadFormContent(
    state: UploadUiState,
    viewModel: UploadViewModel,
    filePicker: PlatformFilePicker,
    modifier: Modifier = Modifier
) {
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = NeonPurple,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        cursorColor = NeonPurple,
        focusedLabelColor = NeonPurple,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // File selection area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable {
                    filePicker.pickFile("video/*") { result ->
                        if (result != null) {
                            viewModel.selectFile(result.fileName, result.fileBytes)
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
            shape = MaterialTheme.shapes.medium,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NeonViolet.copy(alpha = 0.08f),
                                NeonPurpleDark.copy(alpha = 0.04f),
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (state.selectedFileName != null) Icons.Default.VideoFile else Icons.Default.CloudUpload,
                        contentDescription = "Select file",
                        modifier = Modifier.size(36.dp),
                        tint = NeonPurple,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.selectedFileName ?: "Tap to select a video file",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (state.selectedFileName != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        if (state.fileError != null) {
            Text(
                text = state.fileError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Title
        OutlinedTextField(
            value = state.title,
            onValueChange = { viewModel.updateTitle(it) },
            label = { Text("Title *") },
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            singleLine = true,
            colors = textFieldColors,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        OutlinedTextField(
            value = state.description,
            onValueChange = { viewModel.updateDescription(it) },
            label = { Text("Description") },
            minLines = 3,
            maxLines = 5,
            colors = textFieldColors,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category dropdown
        var categoryExpanded by remember { mutableStateOf(false) }
        val selectedCategoryName = viewModel.categories
            .firstOrNull { it.id == state.selectedCategoryId }?.name ?: ""

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategoryName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Category *") },
                isError = state.categoryError != null,
                supportingText = state.categoryError?.let { { Text(it) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                colors = textFieldColors,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = categoryExpanded,
                onDismissRequest = { categoryExpanded = false }
            ) {
                viewModel.categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category.name) },
                        onClick = {
                            viewModel.selectCategory(category.id)
                            categoryExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Difficulty selector
        if (state.isLicensedCreator) {
            Text(
                text = "Difficulty",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                viewModel.difficulties.forEach { difficulty ->
                    FilterChip(
                        selected = state.selectedDifficulty == difficulty,
                        onClick = { viewModel.selectDifficulty(difficulty) },
                        label = {
                            Text(difficulty.lowercase().replaceFirstChar { it.uppercase() })
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.2f),
                            selectedLabelColor = NeonPurpleBright,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Error message
        if (state.error != null) {
            Text(
                text = state.error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Upload progress
        if (state.isUploading) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = NeonPurple,
                trackColor = NeonPurple.copy(alpha = 0.15f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Uploading...",
                style = MaterialTheme.typography.bodySmall,
                color = NeonPurple.copy(alpha = 0.7f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Upload button
        Button(
            onClick = { viewModel.upload() },
            enabled = !state.isUploading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonPurple,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            if (state.isUploading) {
                Text("Uploading...")
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Upload",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
