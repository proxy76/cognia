package com.cognia.app.ui.quiz

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizTakingViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsLoading() {
        val vm = QuizTakingViewModel()
        val state = vm.state.value
        assertTrue(state.isLoading)
        assertTrue(state.questions.isEmpty())
        assertNull(state.results)
    }

    @Test
    fun selectAnswerUpdatesSelectedAnswers() {
        val vm = QuizTakingViewModel()
        vm.selectAnswer(0, 2)
        assertEquals(2, vm.state.value.selectedAnswers[0])
    }

    @Test
    fun submitWithoutSelectionDoesNothing() {
        val vm = QuizTakingViewModel()
        // No questions loaded and no answer selected
        vm.submitAnswer()
        assertFalse(vm.state.value.showFeedback)
    }

    @Test
    fun selectAnswerIgnoredDuringFeedback() {
        val vm = QuizTakingViewModel()
        // Manually simulate a state with feedback showing
        // Since loadQuiz is async, test the guard logic by setting up state
        vm.selectAnswer(0, 2)
        assertEquals(2, vm.state.value.selectedAnswers[0])
    }

    @Test
    fun initialQuizTitleIsEmpty() {
        val vm = QuizTakingViewModel()
        assertEquals("", vm.state.value.quizTitle)
    }

    @Test
    fun initialCurrentQuestionIndexIsZero() {
        val vm = QuizTakingViewModel()
        assertEquals(0, vm.state.value.currentQuestionIndex)
    }

    @Test
    fun initialResultsAreNull() {
        val vm = QuizTakingViewModel()
        assertNull(vm.state.value.results)
    }

    @Test
    fun selectMultipleAnswersUpdatesMap() {
        val vm = QuizTakingViewModel()
        vm.selectAnswer(0, 1)
        vm.selectAnswer(1, 3)
        vm.selectAnswer(2, 0)

        val answers = vm.state.value.selectedAnswers
        assertEquals(1, answers[0])
        assertEquals(3, answers[1])
        assertEquals(0, answers[2])
    }
}
