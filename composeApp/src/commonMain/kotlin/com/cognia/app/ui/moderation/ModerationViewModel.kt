package com.cognia.app.ui.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.dto.moderation.LicenseRequestResponse
import com.cognia.app.dto.moderation.ModerationQueueItem
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModerationUiState(
    val isLoading: Boolean = true,
    val queueItems: List<ModerationQueueItem> = emptyList(),
    val licenseRequests: List<LicenseRequestResponse> = emptyList(),
    val error: String? = null,
    val actionInProgress: String? = null, // reviewId currently being actioned
    val actionSuccess: String? = null
)

class ModerationViewModel : ViewModel() {
    private val _state = MutableStateFlow(ModerationUiState())
    val state: StateFlow<ModerationUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadQueue()
        loadLicenseRequests()
    }

    fun loadQueue() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = api.getModerationQueue()) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        queueItems = result.data.items
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Failed to load queue: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Network error: ${result.throwable.message}"
                    )
                }
            }
        }
    }

    fun loadLicenseRequests() {
        viewModelScope.launch {
            when (val result = api.getLicenseRequests()) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(licenseRequests = result.data.items)
                }
                is ApiResult.Error -> { /* silently fail for now */ }
                is ApiResult.NetworkError -> { /* silently fail for now */ }
            }
        }
    }

    fun approveReview(reviewId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = reviewId, actionSuccess = null)
            when (val result = api.decideModerationReview(reviewId, "APPROVED")) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = "Content approved",
                        queueItems = _state.value.queueItems.filter { it.reviewId != reviewId }
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Approve failed: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Network error"
                    )
                }
            }
        }
    }

    fun rejectReview(reviewId: String, reason: String?, issueStrike: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = reviewId, actionSuccess = null)
            when (val result = api.decideModerationReview(reviewId, "REJECTED", reason, issueStrike)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = "Content rejected",
                        queueItems = _state.value.queueItems.filter { it.reviewId != reviewId }
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Reject failed: ${result.message}"
                    )
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        error = "Network error"
                    )
                }
            }
        }
    }

    fun approveLicense(requestId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = requestId, actionSuccess = null)
            when (val result = api.approveLicenseRequest(requestId)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = "License approved",
                        licenseRequests = _state.value.licenseRequests.map {
                            if (it.id == requestId) result.data else it
                        }
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(actionInProgress = null, error = "Failed: ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(actionInProgress = null, error = "Network error")
                }
            }
        }
    }

    fun rejectLicense(requestId: String, reason: String?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(actionInProgress = requestId, actionSuccess = null)
            when (val result = api.rejectLicenseRequest(requestId, reason)) {
                is ApiResult.Success -> {
                    _state.value = _state.value.copy(
                        actionInProgress = null,
                        actionSuccess = "License rejected",
                        licenseRequests = _state.value.licenseRequests.map {
                            if (it.id == requestId) result.data else it
                        }
                    )
                }
                is ApiResult.Error -> {
                    _state.value = _state.value.copy(actionInProgress = null, error = "Failed: ${result.message}")
                }
                is ApiResult.NetworkError -> {
                    _state.value = _state.value.copy(actionInProgress = null, error = "Network error")
                }
            }
        }
    }

    fun clearActionSuccess() {
        _state.value = _state.value.copy(actionSuccess = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
