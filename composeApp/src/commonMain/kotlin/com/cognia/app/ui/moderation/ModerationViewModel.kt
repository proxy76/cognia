package com.cognia.app.ui.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.dto.moderation.LicenseRequestResponse
import com.cognia.app.dto.moderation.ModerationQueueItem
import com.cognia.app.dto.moderation.ReportResponse
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ModerationUiState(
    val pendingReviews: List<ModerationQueueItem> = emptyList(),
    val reports: List<ReportResponse> = emptyList(),
    val licenseRequests: List<LicenseRequestResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ModerationViewModel : ViewModel() {
    private val _state = MutableStateFlow(ModerationUiState())
    val state: StateFlow<ModerationUiState> = _state.asStateFlow()

    private val api get() = ApiClientProvider.client

    init {
        loadAll()
    }

    fun loadAll() {
        loadPendingReviews()
        loadReports()
        loadLicenseRequests()
    }

    private fun loadPendingReviews() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = api.getModerationQueue()) {
                is ApiResult.Success -> _state.value = _state.value.copy(
                    pendingReviews = result.data.items,
                    isLoading = false,
                    error = null
                )
                is ApiResult.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
                is ApiResult.NetworkError -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Network error: ${result.throwable.message}"
                )
            }
        }
    }

    private fun loadReports() {
        viewModelScope.launch {
            when (val result = api.getReports()) {
                is ApiResult.Success -> _state.value = _state.value.copy(reports = result.data.items)
                is ApiResult.Error -> { /* silently skip — pending reviews tab is primary */ }
                is ApiResult.NetworkError -> { }
            }
        }
    }

    private fun loadLicenseRequests() {
        viewModelScope.launch {
            when (val result = api.getLicenseRequests()) {
                is ApiResult.Success -> _state.value = _state.value.copy(licenseRequests = result.data.items)
                is ApiResult.Error -> { }
                is ApiResult.NetworkError -> { }
            }
        }
    }

    fun approveReview(reviewId: String) {
        viewModelScope.launch {
            when (api.decideModerationReview(reviewId, "APPROVED")) {
                is ApiResult.Success -> loadPendingReviews()
                is ApiResult.Error -> { }
                is ApiResult.NetworkError -> { }
            }
        }
    }

    fun rejectReview(reviewId: String, reason: String? = null, issueStrike: Boolean = false) {
        viewModelScope.launch {
            when (api.decideModerationReview(reviewId, "REJECTED", reason, issueStrike)) {
                is ApiResult.Success -> loadPendingReviews()
                is ApiResult.Error -> { }
                is ApiResult.NetworkError -> { }
            }
        }
    }

    fun approveLicense(id: String) {
        viewModelScope.launch {
            when (api.approveLicenseRequest(id)) {
                is ApiResult.Success -> loadLicenseRequests()
                is ApiResult.Error -> { }
                is ApiResult.NetworkError -> { }
            }
        }
    }

    fun rejectLicense(id: String, reason: String? = null) {
        viewModelScope.launch {
            when (api.rejectLicenseRequest(id, reason)) {
                is ApiResult.Success -> loadLicenseRequests()
                is ApiResult.Error -> { }
                is ApiResult.NetworkError -> { }
            }
        }
    }
}
