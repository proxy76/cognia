package com.cognia.app.ui.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CategoryItem(
    val id: String,
    val name: String,
    val isRecommended: Boolean = false
)

data class OnboardingUiState(
    val currentStep: Int = 0, // 0=description, 1=recommendations, 2=confirm
    val selfDescription: String = "",
    val isLoadingRecommendations: Boolean = false,
    val availableCategories: List<CategoryItem> = emptyList(),
    val selectedCategoryIds: Set<String> = emptySet(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val isComplete: Boolean = false
)

class OnboardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(OnboardingUiState())
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    fun updateSelfDescription(text: String) {
        _state.value = _state.value.copy(selfDescription = text, error = null)
    }

    fun nextStep() {
        val current = _state.value
        if (current.currentStep < 2) {
            _state.value = current.copy(currentStep = current.currentStep + 1, error = null)
            if (_state.value.currentStep == 1 && _state.value.availableCategories.isEmpty()) {
                fetchRecommendations()
            }
        }
    }

    fun previousStep() {
        val current = _state.value
        if (current.currentStep > 0) {
            _state.value = current.copy(currentStep = current.currentStep - 1, error = null)
        }
    }

    fun fetchRecommendations() {
        _state.value = _state.value.copy(isLoadingRecommendations = true, error = null)

        // TODO: Wire to actual API call using RecommendRequest/RecommendResponse DTOs
        // For now, simulate with mock data
        val mockCategories = listOf(
            CategoryItem("1", "Science", isRecommended = true),
            CategoryItem("2", "Mathematics", isRecommended = true),
            CategoryItem("3", "Technology", isRecommended = true),
            CategoryItem("4", "Art", isRecommended = true),
            CategoryItem("5", "Music", isRecommended = true),
            CategoryItem("6", "History", isRecommended = false),
            CategoryItem("7", "Literature", isRecommended = false),
            CategoryItem("8", "Physics", isRecommended = true),
            CategoryItem("9", "Chemistry", isRecommended = false),
            CategoryItem("10", "Biology", isRecommended = false),
            CategoryItem("11", "Computer Science", isRecommended = true),
            CategoryItem("12", "Psychology", isRecommended = false),
            CategoryItem("13", "Philosophy", isRecommended = false),
            CategoryItem("14", "Economics", isRecommended = false),
            CategoryItem("15", "Languages", isRecommended = false),
            CategoryItem("16", "Health", isRecommended = false),
            CategoryItem("17", "Geography", isRecommended = false),
            CategoryItem("18", "Engineering", isRecommended = true),
            CategoryItem("19", "Business", isRecommended = false),
            CategoryItem("20", "Environment", isRecommended = false)
        )

        // Pre-select recommended categories
        val recommendedIds = mockCategories.filter { it.isRecommended }.map { it.id }.toSet()

        _state.value = _state.value.copy(
            isLoadingRecommendations = false,
            availableCategories = mockCategories,
            selectedCategoryIds = recommendedIds
        )
    }

    fun toggleCategory(id: String) {
        val current = _state.value
        val newSelected = if (id in current.selectedCategoryIds) {
            current.selectedCategoryIds - id
        } else {
            current.selectedCategoryIds + id
        }
        _state.value = current.copy(selectedCategoryIds = newSelected)
    }

    fun savePreferences() {
        _state.value = _state.value.copy(isSaving = true, error = null)

        // TODO: Wire to actual API call using SavePreferencesRequest DTO
        // For now, simulate success
        _state.value = _state.value.copy(
            isSaving = false,
            isComplete = true
        )
    }
}
