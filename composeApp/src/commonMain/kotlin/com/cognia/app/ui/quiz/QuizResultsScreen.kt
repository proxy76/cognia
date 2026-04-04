package com.cognia.app.ui.quiz

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cognia.app.ui.theme.NeonCyan
import com.cognia.app.ui.theme.NeonPurple
import com.cognia.app.ui.theme.NeonPurpleBright

private val CorrectGreen = Color(0xFF4CAF50)
private val IncorrectRed = Color(0xFFF44336)
private val GoldTrophy = Color(0xFFFFD700)

@Composable
fun QuizResultsScreen(
    results: QuizResults,
    questions: List<QuizQuestionItem>,
    selectedAnswers: Map<Int, Int>,
    onDone: () -> Unit
) {
    val isPerfectScore = results.score == results.totalQuestions

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Trophy icon for perfect score
        if (isPerfectScore) {
            Icon(
                imageVector = Icons.Filled.EmojiEvents,
                contentDescription = "Trophy",
                modifier = Modifier.size(64.dp),
                tint = GoldTrophy,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Perfect Score!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = GoldTrophy,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Score display
        Text(
            text = "${results.score}/${results.totalQuestions} Correct!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Points awarded
        Text(
            text = "+${results.pointsAwarded} points",
            style = MaterialTheme.typography.titleLarge,
            color = NeonCyan,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Per-question summary
        Text(
            text = "Question Summary",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(12.dp))

        questions.forEachIndexed { index, question ->
            val isCorrect = results.questionResults.getOrNull(index) == true
            val selectedIndex = selectedAnswers[index]
            val selectedText = if (selectedIndex != null && selectedIndex in question.options.indices) {
                question.options[selectedIndex]
            } else {
                "No answer"
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) {
                        CorrectGreen.copy(alpha = 0.08f)
                    } else {
                        IncorrectRed.copy(alpha = 0.08f)
                    }
                ),
                shape = MaterialTheme.shapes.small,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                        contentDescription = if (isCorrect) "Correct" else "Incorrect",
                        tint = if (isCorrect) CorrectGreen else IncorrectRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = question.questionText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (!isCorrect) {
                            Text(
                                text = "Your answer: $selectedText",
                                style = MaterialTheme.typography.bodySmall,
                                color = IncorrectRed,
                            )
                            Text(
                                text = "Correct: ${question.options[question.correctIndex]}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CorrectGreen,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(
                "Done",
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
