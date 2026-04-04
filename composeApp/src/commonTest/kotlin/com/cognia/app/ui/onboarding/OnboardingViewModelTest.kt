package com.cognia.app.ui.onboarding

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @Test
    fun initialStateHasStepZeroAndEmptyDescription() {
        val viewModel = OnboardingViewModel()
        val state = viewModel.state.value

        assertEquals(0, state.currentStep)
        assertEquals("", state.selfDescription)
        assertTrue(state.availableCategories.isEmpty())
        assertTrue(state.selectedCategoryIds.isEmpty())
        assertFalse(state.isLoadingRecommendations)
        assertFalse(state.isSaving)
        assertFalse(state.isComplete)
        assertEquals(null, state.error)
    }

    @Test
    fun updateSelfDescriptionUpdatesState() {
        val viewModel = OnboardingViewModel()

        viewModel.updateSelfDescription("I love science and math")

        assertEquals("I love science and math", viewModel.state.value.selfDescription)
    }

    @Test
    fun nextStepIncrementsCurrentStep() {
        val viewModel = OnboardingViewModel()

        assertEquals(0, viewModel.state.value.currentStep)

        viewModel.nextStep()
        assertEquals(1, viewModel.state.value.currentStep)

        viewModel.nextStep()
        assertEquals(2, viewModel.state.value.currentStep)
    }

    @Test
    fun nextStepDoesNotExceedMaxStep() {
        val viewModel = OnboardingViewModel()

        viewModel.nextStep() // 1
        viewModel.nextStep() // 2
        viewModel.nextStep() // should stay at 2

        assertEquals(2, viewModel.state.value.currentStep)
    }

    @Test
    fun previousStepDecrementsCurrentStep() {
        val viewModel = OnboardingViewModel()

        viewModel.nextStep() // 1
        viewModel.nextStep() // 2

        viewModel.previousStep()
        assertEquals(1, viewModel.state.value.currentStep)

        viewModel.previousStep()
        assertEquals(0, viewModel.state.value.currentStep)
    }

    @Test
    fun previousStepDoesNotGoBelowZero() {
        val viewModel = OnboardingViewModel()

        viewModel.previousStep() // should stay at 0

        assertEquals(0, viewModel.state.value.currentStep)
    }

    @Test
    fun toggleCategoryAddsToSelectedIds() {
        val viewModel = OnboardingViewModel()
        viewModel.fetchRecommendations()

        // Clear pre-selected recommendations
        val preSelected = viewModel.state.value.selectedCategoryIds.toSet()

        // Toggle off a pre-selected one
        val firstSelected = preSelected.first()
        viewModel.toggleCategory(firstSelected)
        assertFalse(firstSelected in viewModel.state.value.selectedCategoryIds)

        // Toggle it back on
        viewModel.toggleCategory(firstSelected)
        assertTrue(firstSelected in viewModel.state.value.selectedCategoryIds)
    }

    @Test
    fun toggleCategoryRemovesFromSelectedIds() {
        val viewModel = OnboardingViewModel()
        viewModel.fetchRecommendations()

        // A recommended category should be pre-selected
        val selectedId = viewModel.state.value.selectedCategoryIds.first()

        viewModel.toggleCategory(selectedId)

        assertFalse(selectedId in viewModel.state.value.selectedCategoryIds)
    }

    @Test
    fun fetchRecommendationsPopulatesCategories() {
        val viewModel = OnboardingViewModel()

        viewModel.fetchRecommendations()

        val state = viewModel.state.value
        assertFalse(state.isLoadingRecommendations)
        assertTrue(state.availableCategories.isNotEmpty())
        assertEquals(20, state.availableCategories.size)
        assertTrue(state.selectedCategoryIds.isNotEmpty())

        // Verify recommended categories are pre-selected
        val recommendedIds = state.availableCategories
            .filter { it.isRecommended }
            .map { it.id }
            .toSet()
        assertEquals(recommendedIds, state.selectedCategoryIds)
    }

    @Test
    fun savePreferencesSetsComplete() {
        val viewModel = OnboardingViewModel()

        viewModel.savePreferences()

        val state = viewModel.state.value
        assertFalse(state.isSaving)
        assertTrue(state.isComplete)
    }

    @Test
    fun nextStepToRecommendationsAutoFetches() {
        val viewModel = OnboardingViewModel()

        // Going to step 1 with empty categories should trigger fetch
        viewModel.nextStep()

        val state = viewModel.state.value
        assertEquals(1, state.currentStep)
        assertTrue(state.availableCategories.isNotEmpty())
    }
}
