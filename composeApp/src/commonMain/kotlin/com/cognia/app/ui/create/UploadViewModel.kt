package com.cognia.app.ui.create

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UploadUiState(
    val selectedFileName: String? = null,
    val title: String = "",
    val description: String = "",
    val selectedCategoryId: String? = null,
    val selectedDifficulty: String? = null,
    val isLicensedCreator: Boolean = false,
    val titleError: String? = null,
    val categoryError: String? = null,
    val fileError: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: Float = 0f,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

data class CategoryOption(val id: String, val name: String)

class UploadViewModel : ViewModel() {
    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    val categories = listOf(
        CategoryOption("1", "Science"),
        CategoryOption("2", "Mathematics"),
        CategoryOption("3", "Technology"),
        CategoryOption("4", "Art"),
        CategoryOption("5", "Music"),
        CategoryOption("6", "History"),
        CategoryOption("7", "Literature"),
        CategoryOption("8", "Computer Science")
    )

    val difficulties = listOf("EASY", "MEDIUM", "HARD")

    fun selectFile(fileName: String) {
        _state.value = _state.value.copy(
            selectedFileName = fileName,
            fileError = null,
            error = null
        )
    }

    fun updateTitle(title: String) {
        _state.value = _state.value.copy(
            title = title,
            titleError = null,
            error = null
        )
    }

    fun updateDescription(desc: String) {
        _state.value = _state.value.copy(description = desc, error = null)
    }

    fun selectCategory(id: String) {
        _state.value = _state.value.copy(
            selectedCategoryId = id,
            categoryError = null,
            error = null
        )
    }

    fun selectDifficulty(difficulty: String) {
        _state.value = _state.value.copy(selectedDifficulty = difficulty, error = null)
    }

    fun upload() {
        val current = _state.value

        var hasError = false
        var titleError: String? = null
        var categoryError: String? = null
        var fileError: String? = null

        if (current.title.isBlank()) {
            titleError = "Title is required"
            hasError = true
        }
        if (current.selectedCategoryId == null) {
            categoryError = "Please select a category"
            hasError = true
        }
        if (current.selectedFileName == null) {
            fileError = "Please select a file"
            hasError = true
        }

        if (hasError) {
            _state.value = current.copy(
                titleError = titleError,
                categoryError = categoryError,
                fileError = fileError
            )
            return
        }

        _state.value = current.copy(isUploading = true, error = null)

        // TODO: Wire to actual API
        // Simulate success
        _state.value = _state.value.copy(
            isUploading = false,
            uploadProgress = 1f,
            uploadSuccess = true
        )
    }

    fun clearState() {
        _state.value = UploadUiState()
    }
}
