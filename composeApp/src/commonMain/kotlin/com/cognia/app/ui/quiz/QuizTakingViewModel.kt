package com.cognia.app.ui.quiz

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuizTakingState(
    val quizTitle: String = "",
    val questions: List<QuizQuestionItem> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswers: Map<Int, Int> = emptyMap(),
    val showFeedback: Boolean = false,
    val isCorrect: Boolean = false,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val results: QuizResults? = null,
    val error: String? = null
)

data class QuizQuestionItem(
    val id: String,
    val questionText: String,
    val options: List<String>,
    val correctIndex: Int
)

data class QuizResults(
    val score: Int,
    val totalQuestions: Int,
    val pointsAwarded: Int,
    val questionResults: List<Boolean>
)

class QuizTakingViewModel : ViewModel() {
    private val _state = MutableStateFlow(QuizTakingState())
    val state: StateFlow<QuizTakingState> = _state.asStateFlow()

    fun loadQuiz(quizId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        // TODO: Wire to actual API using QuizResponse DTO
        // Mock data for now
        val mockQuestions = listOf(
            QuizQuestionItem(
                id = "q1",
                questionText = "What is the capital of France?",
                options = listOf("London", "Berlin", "Paris", "Madrid"),
                correctIndex = 2
            ),
            QuizQuestionItem(
                id = "q2",
                questionText = "Which planet is known as the Red Planet?",
                options = listOf("Venus", "Mars", "Jupiter", "Saturn"),
                correctIndex = 1
            ),
            QuizQuestionItem(
                id = "q3",
                questionText = "What is the largest mammal?",
                options = listOf("Elephant", "Blue Whale", "Giraffe", "Hippopotamus"),
                correctIndex = 1
            ),
            QuizQuestionItem(
                id = "q4",
                questionText = "Who painted the Mona Lisa?",
                options = listOf("Van Gogh", "Picasso", "Da Vinci", "Monet"),
                correctIndex = 2
            ),
            QuizQuestionItem(
                id = "q5",
                questionText = "What is the chemical symbol for water?",
                options = listOf("O2", "CO2", "H2O", "NaCl"),
                correctIndex = 2
            )
        )

        _state.value = _state.value.copy(
            isLoading = false,
            quizTitle = "General Knowledge Quiz",
            questions = mockQuestions,
            currentQuestionIndex = 0,
            selectedAnswers = emptyMap(),
            showFeedback = false,
            results = null
        )
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        if (_state.value.showFeedback) return
        _state.value = _state.value.copy(
            selectedAnswers = _state.value.selectedAnswers + (questionIndex to optionIndex)
        )
    }

    fun submitAnswer() {
        val current = _state.value
        val questionIndex = current.currentQuestionIndex
        val selectedOption = current.selectedAnswers[questionIndex] ?: return

        val question = current.questions[questionIndex]
        val correct = selectedOption == question.correctIndex

        _state.value = current.copy(
            showFeedback = true,
            isCorrect = correct
        )
    }

    fun nextQuestion() {
        val current = _state.value
        val nextIndex = current.currentQuestionIndex + 1

        if (nextIndex >= current.questions.size) {
            // Calculate results
            val questionResults = current.questions.mapIndexed { index, question ->
                current.selectedAnswers[index] == question.correctIndex
            }
            val score = questionResults.count { it }
            val pointsPerQuestion = 20

            _state.value = current.copy(
                showFeedback = false,
                results = QuizResults(
                    score = score,
                    totalQuestions = current.questions.size,
                    pointsAwarded = score * pointsPerQuestion,
                    questionResults = questionResults
                )
            )
        } else {
            _state.value = current.copy(
                currentQuestionIndex = nextIndex,
                showFeedback = false
            )
        }
    }
}
