package com.cognia.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

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

        viewModelScope.launch {
            // Try AI recommendations first, fall back to loading all categories
            val selfDesc = _state.value.selfDescription
            val recommendedIds = mutableSetOf<String>()

            if (selfDesc.isNotBlank()) {
                when (val recResult = api.getRecommendations(selfDesc)) {
                    is ApiResult.Success -> {
                        recommendedIds.addAll(recResult.data.recommendedCategories.map { it.id })
                    }
                    else -> { /* Fall back to showing all categories without recommendations */ }
                }
            }

            // Always load the full category list
            when (val catResult = api.getCategories()) {
                is ApiResult.Success -> {
                    val categories = catResult.data.map { cat ->
                        CategoryItem(
                            id = cat.id,
                            name = cat.name,
                            isRecommended = cat.id in recommendedIds
                        )
                    }
                    val preSelected = if (recommendedIds.isNotEmpty()) {
                        recommendedIds
                    } else {
                        emptySet()
                    }
                    _state.value = _state.value.copy(
                        isLoadingRecommendations = false,
                        availableCategories = categories,
                        selectedCategoryIds = preSelected
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoadingRecommendations = false,
                        error = "Failed to load categories: ${catResult.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isLoadingRecommendations = false,
                        error = "Network error loading categories"
                    )
                }
            }
        }
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

        viewModelScope.launch {
            val categoryIds = _state.value.selectedCategoryIds.toList()
            val selfDesc = _state.value.selfDescription.ifBlank { null }
            when (val result = api.savePreferences(categoryIds, selfDesc)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(isSaving = false, isComplete = true)
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(isSaving = false, error = result.message)
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(isSaving = false, error = "Network error saving preferences")
                }
            }
        }
    }
}
