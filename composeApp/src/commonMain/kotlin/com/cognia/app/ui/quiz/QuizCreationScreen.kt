package com.cognia.app.ui.quiz

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.SurfaceDarkCard

private val CorrectGreen = Color(0xFF4CAF50)

@Composable
fun QuizCreationScreen(
    viewModel: QuizCreationViewModel,
    onSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    if (state.submitSuccess) {
        onSuccess()
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Step indicator
        StepIndicator(
            currentStep = state.currentStep,
            totalSteps = 3,
            modifier = Modifier
                .padding(top = 20.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Step content
        AnimatedContent(
            targetState = state.currentStep,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                } else {
                    slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { step ->
            when (step) {
                0 -> MetadataStep(
                    state = state,
                    viewModel = viewModel,
                    onNext = viewModel::nextStep
                )
                1 -> QuestionsStep(
                    state = state,
                    viewModel = viewModel,
                    onNext = viewModel::nextStep,
                    onBack = viewModel::previousStep
                )
                2 -> PreviewStep(
                    state = state,
                    onSubmit = viewModel::submit,
                    onBack = viewModel::previousStep,
                    isSubmitting = state.isSubmitting
                )
            }
        }
    }
}

@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            Box(
                modifier = Modifier
                    .then(
                        if (index == currentStep) Modifier.width(24.dp).height(10.dp)
                        else Modifier.size(10.dp)
                    )
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isActive) NeonPurple
                        else NeonPurple.copy(alpha = 0.2f)
                    )
            )
        }
    }
}

@Composable
private fun cogniaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    cursorColor = NeonPurple,
    focusedLabelColor = NeonPurple,
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MetadataStep(
    state: QuizCreationState,
    viewModel: QuizCreationViewModel,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Create Quiz",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.title,
            onValueChange = viewModel::updateTitle,
            label = { Text("Quiz Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = state.titleError != null,
            supportingText = state.titleError?.let { { Text(it) } },
            singleLine = true,
            colors = cogniaTextFieldColors(),
            shape = MaterialTheme.shapes.small,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Quiz Type",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.quizTypes.forEach { type ->
                FilterChip(
                    selected = state.selectedQuizType == type,
                    onClick = { viewModel.selectQuizType(type) },
                    label = {
                        Text(
                            type.replace("_", " ")
                                .lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Category",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))

        var categoryExpanded by remember { mutableStateOf(false) }
        val selectedCategoryName = viewModel.categories.find { it.id == state.selectedCategoryId }?.name ?: ""

        ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCategoryName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                colors = cogniaTextFieldColors(),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
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

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Difficulty",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            viewModel.difficulties.forEach { difficulty ->
                FilterChip(
                    selected = state.selectedDifficulty == difficulty,
                    onClick = { viewModel.selectDifficulty(difficulty) },
                    label = {
                        Text(
                            difficulty.lowercase()
                                .replaceFirstChar { it.uppercase() }
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = NeonCyan.copy(alpha = 0.15f),
                        selectedLabelColor = NeonCyan,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                    ),
                )
            }
        }

        state.error?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                "Next: Add Questions",
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun QuestionsStep(
    state: QuizCreationState,
    viewModel: QuizCreationViewModel,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Add Questions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        state.questions.forEachIndexed { questionIndex, question ->
            QuestionCard(
                questionIndex = questionIndex,
                question = question,
                canDelete = state.questions.size > 1,
                onQuestionTextChanged = { text -> viewModel.updateQuestionText(questionIndex, text) },
                onOptionTextChanged = { optionIndex, text ->
                    viewModel.updateOptionText(questionIndex, optionIndex, text)
                },
                onCorrectOptionSelected = { optionIndex ->
                    viewModel.setCorrectOption(questionIndex, optionIndex)
                },
                onDelete = { viewModel.removeQuestion(questionIndex) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedButton(
            onClick = viewModel::addQuestion,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = NeonPurple)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Question", color = NeonPurple)
        }

        state.questionsError?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Back")
            }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Next: Preview", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun QuestionCard(
    questionIndex: Int,
    question: EditableQuestion,
    canDelete: Boolean,
    onQuestionTextChanged: (String) -> Unit,
    onOptionTextChanged: (Int, String) -> Unit,
    onCorrectOptionSelected: (Int) -> Unit,
    onDelete: () -> Unit
) {
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
                    text = "Question ${questionIndex + 1}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurpleBright,
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete question",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }

            OutlinedTextField(
                value = question.questionText,
                onValueChange = onQuestionTextChanged,
                label = { Text("Question text") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                colors = cogniaTextFieldColors(),
                shape = MaterialTheme.shapes.small,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Options (select correct answer):",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(4.dp))

            question.options.forEachIndexed { optionIndex, optionText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = question.correctOptionIndex == optionIndex,
                        onClick = { onCorrectOptionSelected(optionIndex) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = NeonCyan,
                            unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    OutlinedTextField(
                        value = optionText,
                        onValueChange = { onOptionTextChanged(optionIndex, it) },
                        label = { Text("Option ${optionIndex + 1}") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = cogniaTextFieldColors(),
                        shape = MaterialTheme.shapes.small,
                    )
                }
            }
        }
    }
}

@Composable
private fun PreviewStep(
    state: QuizCreationState,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    isSubmitting: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quiz metadata
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = NeonPurple.copy(alpha = 0.08f)
            ),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = state.title.ifBlank { "Untitled Quiz" },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Type: ${state.selectedQuizType.replace("_", " ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.selectedDifficulty != null) {
                    Text(
                        text = "Difficulty: ${state.selectedDifficulty}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${state.questions.size} question(s)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonCyan.copy(alpha = 0.7f),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preview questions
        state.questions.forEachIndexed { index, question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDarkCard),
                shape = MaterialTheme.shapes.small,
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Q${index + 1}: ${question.questionText.ifBlank { "(empty)" }}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    question.options.forEachIndexed { optIndex, optText ->
                        val isCorrect = optIndex == question.correctOptionIndex
                        Text(
                            text = "${if (isCorrect) "* " else "  "}${optText.ifBlank { "(empty)" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCorrect) CorrectGreen else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        )
                    }
                }
            }
        }

        state.error?.let { errorText ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = !isSubmitting,
                shape = MaterialTheme.shapes.medium,
            ) {
                Text("Back")
            }
            Button(
                onClick = onSubmit,
                modifier = Modifier.weight(1f).height(52.dp),
                enabled = !isSubmitting,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = MaterialTheme.shapes.medium,
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Publish Quiz", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
