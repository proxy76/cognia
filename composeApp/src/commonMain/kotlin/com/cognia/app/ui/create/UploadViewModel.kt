package com.cognia.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import com.cognia.app.platform.PlatformFilePicker
import com.cognia.app.platform.PickedFile
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
    val uploadedVideoId: String? = null,
    val error: String? = null
)

data class CategoryOption(val id: String, val name: String)

class UploadViewModel : ViewModel() {
    private val _state = MutableStateFlow(UploadUiState())
    val state: StateFlow<UploadUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client
    private val filePicker = PlatformFilePicker()
    private var pickedFile: PickedFile? = null

    private var _categoriesList: List<CategoryOption> = emptyList()
    val categories: List<CategoryOption> get() = _categoriesList

    val difficulties = listOf("EASY", "MEDIUM", "HARD")

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = api.getCategories()) {
                is ApiResult.Success -> {
                    _categoriesList = result.data.map { CategoryOption(it.id, it.name) }
                }
                else -> {}
            }
        }
    }

    fun pickFile() {
        viewModelScope.launch {
            val file = filePicker.pickVideoFile()
            if (file != null) {
                pickedFile = file
                _state.value = _state.value.copy(
                    selectedFileName = file.name,
                    fileError = null,
                    error = null
                )
            }
        }
    }

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
        val file = pickedFile

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
        if (file == null) {
            fileError = "Please select a video file"
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

        _state.value = current.copy(isUploading = true, error = null, uploadProgress = 0.1f)

        viewModelScope.launch {
            // Upload video
            _state.value = _state.value.copy(uploadProgress = 0.3f)

            when (val uploadResult = api.uploadVideo(
                title = current.title,
                description = current.description.ifBlank { null },
                categoryId = current.selectedCategoryId!!,
                fileBytes = file!!.bytes,
                fileName = file.name
            )) {
                is ApiResult.Success -> {
                    val videoId = uploadResult.data.id
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadProgress = 1f,
                        uploadSuccess = true,
                        uploadedVideoId = videoId
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = uploadResult.message
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        uploadProgress = 0f,
                        error = "Network error: ${uploadResult.throwable.message}"
                    )
                }
            }
        }
    }

    fun clearState() {
        pickedFile = null
        _state.value = UploadUiState()
    }
}
