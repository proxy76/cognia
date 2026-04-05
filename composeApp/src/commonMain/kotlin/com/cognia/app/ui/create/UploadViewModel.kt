package com.cognia.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    private val api get() = ApiClientProvider.client

    private var _categoriesList: List<CategoryOption> = emptyList()
    val categories: List<CategoryOption> get() = _categoriesList

    val difficulties = listOf("EASY", "MEDIUM", "HARD")

    /** Holds the raw file bytes selected by the platform file picker. */
    private var selectedFileBytes: ByteArray? = null

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = api.getCategories()) {
                is ApiResult.Success -> {
                    _categoriesList = result.data.map { CategoryOption(it.id, it.name) }
                    _state.value = _state.value.copy(error = _state.value.error)
                }
                else -> {
                    // Categories will be empty until network is available
                }
            }
        }
    }

    /**
     * Called by the platform file picker to store selected file bytes.
     */
    fun selectFile(fileName: String, fileBytes: ByteArray? = null) {
        selectedFileBytes = fileBytes
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
        if (current.selectedFileName == null || selectedFileBytes == null) {
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

        viewModelScope.launch {
            // Step 1: Upload video via multipart
            val uploadResult = api.uploadVideo(
                title = current.title,
                description = current.description.ifBlank { null },
                categoryId = current.selectedCategoryId!!,
                fileBytes = selectedFileBytes!!,
                fileName = current.selectedFileName!!,
            )

            when (uploadResult) {
                is ApiResult.Success -> {
                    val videoId = uploadResult.data.id

                    // Step 2: Auto-publish for licensed creators
                    val publishResult = api.publishVideo(videoId)
                    when (publishResult) {
                        is ApiResult.Success -> {
                            _state.value = _state.value.copy(
                                isUploading = false,
                                uploadProgress = 1f,
                                uploadSuccess = true
                            )
                        }
                        is ApiResult.Error -> {
                            // Upload succeeded but publish failed — video is in DRAFT
                            _state.value = _state.value.copy(
                                isUploading = false,
                                uploadProgress = 1f,
                                uploadSuccess = true,
                                error = "Uploaded but publish failed: ${publishResult.message}"
                            )
                        }
                        is ApiResult.NetworkError -> {
                            _state.value = _state.value.copy(
                                isUploading = false,
                                error = "Network error during publish: ${publishResult.throwable.message}"
                            )
                        }
                    }
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        error = "Upload failed: ${uploadResult.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        error = "Network error: ${uploadResult.throwable.message}"
                    )
                }
            }
        }
    }

    fun clearState() {
        selectedFileBytes = null
        _state.value = UploadUiState()
    }
}
