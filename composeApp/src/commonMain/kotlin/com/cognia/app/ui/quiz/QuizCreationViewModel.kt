package com.cognia.app.ui.quiz

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class QuizCreationState(
    val title: String = "",
    val selectedQuizType: String = "MULTIPLE_CHOICE",
    val selectedCategoryId: String? = null,
    val selectedDifficulty: String? = null,
    val questions: List<EditableQuestion> = listOf(EditableQuestion()),
    val isLicensedCreator: Boolean = false,
    val currentStep: Int = 0,
    val titleError: String? = null,
    val questionsError: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val error: String? = null
)

data class EditableQuestion(
    val questionText: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctOptionIndex: Int = 0
)

data class QuizCategoryOption(val id: String, val name: String)

class QuizCreationViewModel : ViewModel() {
    private val _state = MutableStateFlow(QuizCreationState())
    val state: StateFlow<QuizCreationState> = _state.asStateFlow()

    val categories = listOf(
        QuizCategoryOption("1", "Science"),
        QuizCategoryOption("2", "Mathematics"),
        QuizCategoryOption("3", "Technology"),
        QuizCategoryOption("4", "Art"),
        QuizCategoryOption("5", "Music"),
        QuizCategoryOption("6", "History"),
        QuizCategoryOption("7", "Literature"),
        QuizCategoryOption("8", "Computer Science")
    )

    val quizTypes = listOf("MULTIPLE_CHOICE", "TRUE_FALSE", "FILL_IN_BLANK")

    val difficulties = listOf("EASY", "MEDIUM", "HARD")

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            titleError = null,
            error = null
        )
    }

    fun selectQuizType(type: String) {
        _state.value = _state.value.copy(selectedQuizType = type, error = null)
    }

    fun selectCategory(id: String) {
        _state.value = _state.value.copy(selectedCategoryId = id, error = null)
    }

    fun selectDifficulty(difficulty: String) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty, error = null)
    }

    fun updateQuestionText(index: Int, text: String) {
        val questions = _state.value.questions.toMutableList()
        if (index in questions.indices) {
            questions[index] = questions[index].copy(questionText = text)
            _state.value = _state.value.copy(questions = questions, questionsError = null, error = null)
        }
    }

    fun updateOptionText(questionIndex: Int, optionIndex: Int, text: String) {
        val questions = _state.value.questions.toMutableList()
        if (questionIndex in questions.indices) {
            val question = questions[questionIndex]
            val options = question.options.toMutableList()
            if (optionIndex in options.indices) {
                options[optionIndex] = text
                questions[questionIndex] = question.copy(options = options)
                _state.value = _state.value.copy(questions = questions, questionsError = null, error = null)
            }
        }
    }

    fun setCorrectOption(questionIndex: Int, optionIndex: Int) {
        val questions = _state.value.questions.toMutableList()
        if (questionIndex in questions.indices) {
            questions[questionIndex] = questions[questionIndex].copy(correctOptionIndex = optionIndex)
            _state.value = _state.value.copy(questions = questions, error = null)
        }
    }

    fun addQuestion() {
        _state.value = _state.value.copy(
            questions = _state.value.questions + EditableQuestion(),
            questionsError = null,
            error = null
        )
    }

    fun removeQuestion(index: Int) {
        val questions = _state.value.questions.toMutableList()
        if (index in questions.indices && questions.size > 1) {
            questions.removeAt(index)
            _state.value = _state.value.copy(questions = questions, error = null)
        }
    }

    fun moveQuestion(from: Int, to: Int) {
        val questions = _state.value.questions.toMutableList()
        if (from in questions.indices && to in questions.indices) {
            val item = questions.removeAt(from)
            questions.add(to, item)
            _state.value = _state.value.copy(questions = questions)
        }
    }

    fun nextStep() {
        val current = _state.value
        if (current.currentStep == 0) {
            // Validate metadata
            if (current.title.isBlank()) {
                _state.value = current.copy(titleError = "Title is required")
                return
            }
        }
        if (current.currentStep == 1) {
            // Validate questions
            val validationError = validateQuestions(current.questions)
            if (validationError != null) {
                _state.value = current.copy(questionsError = validationError)
                return
            }
        }
        if (current.currentStep < 2) {
            _state.value = current.copy(currentStep = current.currentStep + 1, error = null)
        }
    }

    fun previousStep() {
        val current = _state.value
        if (current.currentStep > 0) {
            _state.value = current.copy(currentStep = current.currentStep - 1, error = null)
        }
    }

    fun submit() {
        val current = _state.value

        if (current.title.isBlank()) {
            _state.value = current.copy(titleError = "Title is required")
            return
        }

        val validationError = validateQuestions(current.questions)
        if (validationError != null) {
            _state.value = current.copy(questionsError = validationError)
            return
        }

        _state.value = current.copy(isSubmitting = true, error = null)

        // TODO: Wire to actual API using QuizCreateRequest DTO
        // Simulate success
        _state.value = _state.value.copy(
            isSubmitting = false,
            submitSuccess = true
        )
    }

    private fun validateQuestions(questions: List<EditableQuestion>): String? {
        if (questions.isEmpty()) {
            return "At least one question is required"
        }
        for ((index, question) in questions.withIndex()) {
            if (question.questionText.isBlank()) {
                return "Question ${index + 1} needs text"
            }
            val nonEmptyOptions = question.options.count { it.isNotBlank() }
            if (nonEmptyOptions < 2) {
                return "Question ${index + 1} needs at least 2 options"
            }
        }
        return null
    }

    fun clearState() {
        _state.value = QuizCreationState()
    }
}
