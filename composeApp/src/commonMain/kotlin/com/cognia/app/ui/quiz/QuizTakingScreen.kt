package com.cognia.app.ui.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright
import com.cognia.app.ui.theme.SurfaceDarkCard

private val CorrectGreen = Color(0xFF4CAF50)
private val IncorrectRed = Color(0xFFF44336)

@Composable
fun QuizTakingScreen(
    viewModel: QuizTakingViewModel,
    quizId: String,
    onDone: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(quizId) {
        viewModel.loadQuiz(quizId)
    }

    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPurple)
            }
        }
        state.error != null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.error ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadQuiz(quizId) },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    ) {
                        Text("Retry")
                    }
                }
            }
        }
        state.results != null -> {
            QuizResultsScreen(
                results = state.results!!,
                questions = state.questions,
                selectedAnswers = state.selectedAnswers,
                onDone = onDone
            )
        }
        else -> {
            QuizQuestionContent(
                state = state,
                onSelectAnswer = viewModel::selectAnswer,
                onSubmitAnswer = viewModel::submitAnswer,
                onNextQuestion = viewModel::nextQuestion
            )
        }
    }
}

@Composable
private fun QuizQuestionContent(
    state: QuizTakingState,
    onSelectAnswer: (Int, Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onNextQuestion: () -> Unit
) {
    val question = state.questions[state.currentQuestionIndex]
    val selectedOption = state.selectedAnswers[state.currentQuestionIndex]
    val progress = (state.currentQuestionIndex + 1).toFloat() / state.questions.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Progress bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = state.quizTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Question ${state.currentQuestionIndex + 1} of ${state.questions.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonPurple.copy(alpha = 0.7f),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = NeonPurple,
                trackColor = NeonPurple.copy(alpha = 0.15f),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Question text
        Text(
            text = question.questionText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Answer options
        question.options.forEachIndexed { index, option ->
            val isSelected = selectedOption == index
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    state.showFeedback && index == question.correctIndex -> CorrectGreen.copy(alpha = 0.15f)
                    state.showFeedback && isSelected && index != question.correctIndex -> IncorrectRed.copy(alpha = 0.15f)
                    isSelected -> NeonPurple.copy(alpha = 0.12f)
                    else -> SurfaceDarkCard
                }
            )
            val borderColor by animateColorAsState(
                targetValue = when {
                    state.showFeedback && index == question.correctIndex -> CorrectGreen
                    state.showFeedback && isSelected && index != question.correctIndex -> IncorrectRed
                    isSelected -> NeonPurple
                    else -> MaterialTheme.colorScheme.outline
                }
            )

            OutlinedCard(
                onClick = { onSelectAnswer(state.currentQuestionIndex, index) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.outlinedCardColors(
                    containerColor = backgroundColor
                ),
                border = BorderStroke(
                    width = if (isSelected || (state.showFeedback && index == question.correctIndex)) 2.dp else 1.dp,
                    color = borderColor
                ),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = option,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action button
        if (state.showFeedback) {
            Text(
                text = if (state.isCorrect) "Correct!" else "Incorrect! The answer is: ${question.options[question.correctIndex]}",
                style = MaterialTheme.typography.titleMedium,
                color = if (state.isCorrect) CorrectGreen else IncorrectRed,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNextQuestion,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    if (state.currentQuestionIndex < state.questions.size - 1) "Next Question"
                    else "See Results",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        } else {
            Button(
                onClick = onSubmitAnswer,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = selectedOption != null,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    "Submit Answer",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
