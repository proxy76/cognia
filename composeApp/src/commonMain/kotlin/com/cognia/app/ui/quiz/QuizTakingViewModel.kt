package com.cognia.app.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    fun loadQuiz(quizId: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            when (val result = api.getQuiz(quizId)) {
                is ApiResult.Success -> {
                    val quiz = result.data
                    val questions = quiz.questions.map { q ->
                        QuizQuestionItem(
                            id = q.id,
                            questionText = q.questionText,
                            options = q.options.map { it.text },
                            correctIndex = q.correctOptionIndex ?: 0
                        )
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        quizTitle = quiz.title,
                        questions = questions,
                        currentQuestionIndex = 0,
                        selectedAnswers = emptyMap(),
                        showFeedback = false,
                        results = null
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isLoading = false, error = "Network error: ${result.throwable.message}")
                }
            }
        }
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
