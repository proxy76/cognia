package com.cognia.app.ui.onboarding

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

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
    fun toggleCategoryAddsAndRemovesIds() {
        val viewModel = OnboardingViewModel()

        // Toggle a category on
        viewModel.toggleCategory("cat-1")
        assertTrue("cat-1" in viewModel.state.value.selectedCategoryIds)

        // Toggle the same category off
        viewModel.toggleCategory("cat-1")
        assertFalse("cat-1" in viewModel.state.value.selectedCategoryIds)
    }

    @Test
    fun toggleMultipleCategories() {
        val viewModel = OnboardingViewModel()

        viewModel.toggleCategory("cat-1")
        viewModel.toggleCategory("cat-2")
        viewModel.toggleCategory("cat-3")

        val selected = viewModel.state.value.selectedCategoryIds
        assertEquals(3, selected.size)
        assertTrue("cat-1" in selected)
        assertTrue("cat-2" in selected)
        assertTrue("cat-3" in selected)
    }

    @Test
    fun updateSelfDescriptionClearsError() {
        val viewModel = OnboardingViewModel()

        viewModel.updateSelfDescription("new description")

        assertEquals(null, viewModel.state.value.error)
    }
}
