package com.cognia.app.ui.quiz

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun QuizScreen(
    quizId: String,
    onNavigateBack: () -> Unit
) {
    // Container that decides between quiz-taking or quiz-creation based on context.
    // For now, default to quiz-taking.
    val quizTakingViewModel: QuizTakingViewModel = viewModel { QuizTakingViewModel() }
    QuizTakingScreen(
        viewModel = quizTakingViewModel,
        quizId = quizId,
        onDone = onNavigateBack
    )
}
