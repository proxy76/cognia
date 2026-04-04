package com.cognia.app.ui.quiz

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizTakingViewModelTest {

    @Test
    fun initialStateIsLoading() {
        val vm = QuizTakingViewModel()
        val state = vm.state.value
        assertTrue(state.isLoading)
        assertTrue(state.questions.isEmpty())
        assertNull(state.results)
    }

    @Test
    fun afterLoadQuizHasQuestions() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        val state = vm.state.value
        assertFalse(state.isLoading)
        assertTrue(state.questions.isNotEmpty())
        assertEquals(5, state.questions.size)
        assertEquals("General Knowledge Quiz", state.quizTitle)
        assertEquals(0, state.currentQuestionIndex)
    }

    @Test
    fun selectAnswerUpdatesSelectedAnswers() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.selectAnswer(0, 2)
        val state = vm.state.value
        assertEquals(2, state.selectedAnswers[0])
    }

    @Test
    fun submitAnswerShowsFeedback() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.selectAnswer(0, 2) // correct answer for q1
        vm.submitAnswer()
        val state = vm.state.value
        assertTrue(state.showFeedback)
        assertTrue(state.isCorrect)
    }

    @Test
    fun submitAnswerShowsIncorrectFeedback() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.selectAnswer(0, 0) // wrong answer for q1
        vm.submitAnswer()
        val state = vm.state.value
        assertTrue(state.showFeedback)
        assertFalse(state.isCorrect)
    }

    @Test
    fun nextQuestionAdvancesIndex() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.selectAnswer(0, 2)
        vm.submitAnswer()
        vm.nextQuestion()
        val state = vm.state.value
        assertEquals(1, state.currentQuestionIndex)
        assertFalse(state.showFeedback)
        assertNull(state.results)
    }

    @Test
    fun afterLastQuestionResultsAreShown() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")

        // Answer all 5 questions
        for (i in 0 until 5) {
            val question = vm.state.value.questions[i]
            vm.selectAnswer(i, question.correctIndex) // all correct
            vm.submitAnswer()
            vm.nextQuestion()
        }

        val state = vm.state.value
        assertNotNull(state.results)
    }

    @Test
    fun resultsHaveCorrectScore() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")

        // Answer first 3 correctly, last 2 incorrectly
        for (i in 0 until 5) {
            val question = vm.state.value.questions[i]
            if (i < 3) {
                vm.selectAnswer(i, question.correctIndex)
            } else {
                // Pick wrong answer
                val wrongIndex = (question.correctIndex + 1) % question.options.size
                vm.selectAnswer(i, wrongIndex)
            }
            vm.submitAnswer()
            vm.nextQuestion()
        }

        val results = vm.state.value.results
        assertNotNull(results)
        assertEquals(3, results.score)
        assertEquals(5, results.totalQuestions)
        assertEquals(60, results.pointsAwarded) // 3 * 20
        assertEquals(5, results.questionResults.size)
        assertTrue(results.questionResults[0])
        assertTrue(results.questionResults[1])
        assertTrue(results.questionResults[2])
        assertFalse(results.questionResults[3])
        assertFalse(results.questionResults[4])
    }

    @Test
    fun submitWithoutSelectionDoesNothing() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.submitAnswer() // no answer selected
        val state = vm.state.value
        assertFalse(state.showFeedback)
    }

    @Test
    fun selectAnswerIgnoredDuringFeedback() {
        val vm = QuizTakingViewModel()
        vm.loadQuiz("test-quiz-1")
        vm.selectAnswer(0, 2)
        vm.submitAnswer()
        assertTrue(vm.state.value.showFeedback)
        vm.selectAnswer(0, 0) // try to change answer during feedback
        assertEquals(2, vm.state.value.selectedAnswers[0]) // should not change
    }
}
