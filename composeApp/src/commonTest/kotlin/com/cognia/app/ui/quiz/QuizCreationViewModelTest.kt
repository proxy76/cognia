package com.cognia.app.ui.quiz

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizCreationViewModelTest {

    @Test
    fun initialStateHasOneEmptyQuestion() {
        val vm = QuizCreationViewModel()
        val state = vm.state.value
        assertEquals(1, state.questions.size)
        assertEquals("", state.questions[0].questionText)
        assertEquals(4, state.questions[0].options.size)
        assertEquals(0, state.currentStep)
    }

    @Test
    fun updateTitleUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        assertEquals("My Quiz", vm.state.value.title)
        assertNull(vm.state.value.titleError)
    }

    @Test
    fun addQuestionAddsNewQuestion() {
        val vm = QuizCreationViewModel()
        vm.addQuestion()
        assertEquals(2, vm.state.value.questions.size)
    }

    @Test
    fun removeQuestionRemovesQuestion() {
        val vm = QuizCreationViewModel()
        vm.addQuestion()
        assertEquals(2, vm.state.value.questions.size)
        vm.removeQuestion(0)
        assertEquals(1, vm.state.value.questions.size)
    }

    @Test
    fun removeQuestionDoesNotRemoveLastQuestion() {
        val vm = QuizCreationViewModel()
        assertEquals(1, vm.state.value.questions.size)
        vm.removeQuestion(0) // should not remove the only question
        assertEquals(1, vm.state.value.questions.size)
    }

    @Test
    fun setCorrectOptionUpdatesCorrectAnswer() {
        val vm = QuizCreationViewModel()
        vm.setCorrectOption(0, 2)
        assertEquals(2, vm.state.value.questions[0].correctOptionIndex)
    }

    @Test
    fun updateQuestionTextUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.updateQuestionText(0, "What is 2+2?")
        assertEquals("What is 2+2?", vm.state.value.questions[0].questionText)
    }

    @Test
    fun updateOptionTextUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.updateOptionText(0, 0, "Option A")
        assertEquals("Option A", vm.state.value.questions[0].options[0])
    }

    @Test
    fun submitWithMissingTitleShowsError() {
        val vm = QuizCreationViewModel()
        // Leave title blank, add valid question
        vm.updateQuestionText(0, "Test question?")
        vm.updateOptionText(0, 0, "A")
        vm.updateOptionText(0, 1, "B")
        vm.submit()
        assertNotNull(vm.state.value.titleError)
        assertFalse(vm.state.value.submitSuccess)
    }

    @Test
    fun submitWithEmptyQuestionsShowsError() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        // Leave question text empty
        vm.submit()
        assertNotNull(vm.state.value.questionsError)
        assertFalse(vm.state.value.submitSuccess)
    }

    @Test
    fun submitWithTooFewOptionsShowsError() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        vm.updateQuestionText(0, "Test question?")
        vm.updateOptionText(0, 0, "Only one option")
        // Only 1 non-blank option
        vm.submit()
        assertNotNull(vm.state.value.questionsError)
        assertFalse(vm.state.value.submitSuccess)
    }

    @Test
    fun validQuizSubmitSucceeds() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        vm.updateQuestionText(0, "What is 2+2?")
        vm.updateOptionText(0, 0, "3")
        vm.updateOptionText(0, 1, "4")
        vm.setCorrectOption(0, 1)
        vm.submit()
        assertTrue(vm.state.value.submitSuccess)
        assertFalse(vm.state.value.isSubmitting)
    }

    @Test
    fun selectQuizTypeUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.selectQuizType("TRUE_FALSE")
        assertEquals("TRUE_FALSE", vm.state.value.selectedQuizType)
    }

    @Test
    fun selectCategoryUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.selectCategory("3")
        assertEquals("3", vm.state.value.selectedCategoryId)
    }

    @Test
    fun selectDifficultyUpdatesState() {
        val vm = QuizCreationViewModel()
        vm.selectDifficulty("HARD")
        assertEquals("HARD", vm.state.value.selectedDifficulty)
    }

    @Test
    fun nextStepWithBlankTitleShowsError() {
        val vm = QuizCreationViewModel()
        assertEquals(0, vm.state.value.currentStep)
        vm.nextStep() // title is blank
        assertEquals(0, vm.state.value.currentStep) // should not advance
        assertNotNull(vm.state.value.titleError)
    }

    @Test
    fun nextStepWithValidTitleAdvances() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        vm.nextStep()
        assertEquals(1, vm.state.value.currentStep)
    }

    @Test
    fun previousStepGoesBack() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        vm.nextStep()
        assertEquals(1, vm.state.value.currentStep)
        vm.previousStep()
        assertEquals(0, vm.state.value.currentStep)
    }

    @Test
    fun moveQuestionReorders() {
        val vm = QuizCreationViewModel()
        vm.updateQuestionText(0, "First")
        vm.addQuestion()
        vm.updateQuestionText(1, "Second")
        vm.moveQuestion(0, 1)
        assertEquals("Second", vm.state.value.questions[0].questionText)
        assertEquals("First", vm.state.value.questions[1].questionText)
    }

    @Test
    fun clearStateResetsEverything() {
        val vm = QuizCreationViewModel()
        vm.updateTitle("My Quiz")
        vm.addQuestion()
        vm.clearState()
        val state = vm.state.value
        assertEquals("", state.title)
        assertEquals(1, state.questions.size)
        assertEquals(0, state.currentStep)
    }
}
