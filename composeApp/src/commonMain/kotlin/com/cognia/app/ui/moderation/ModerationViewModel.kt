package com.cognia.app.ui.moderation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cognia.app.dto.moderation.*
import com.cognia.app.network.ApiClientProvider
import com.cognia.app.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ModerationViewModel : ViewModel() {

    private val api get() = ApiClientProvider.client

    // ── Pending Reviews ─────────────────────────────────────────────
    private val _queueItems = MutableStateFlow<List<ModerationQueueItem>>(emptyList())
    val queueItems: StateFlow<List<ModerationQueueItem>> = _queueItems.asStateFlow()

    private val _queueLoading = MutableStateFlow(false)
    val queueLoading: StateFlow<Boolean> = _queueLoading.asStateFlow()

    // ── Reports ─────────────────────────────────────────────────────
    private val _reports = MutableStateFlow<List<ReportResponse>>(emptyList())
    val reports: StateFlow<List<ReportResponse>> = _reports.asStateFlow()

    private val _reportsLoading = MutableStateFlow(false)
    val reportsLoading: StateFlow<Boolean> = _reportsLoading.asStateFlow()

    // ── License Requests ────────────────────────────────────────────
    private val _licenseRequests = MutableStateFlow<List<LicenseRequestResponse>>(emptyList())
    val licenseRequests: StateFlow<List<LicenseRequestResponse>> = _licenseRequests.asStateFlow()

    private val _licenseLoading = MutableStateFlow(false)
    val licenseLoading: StateFlow<Boolean> = _licenseLoading.asStateFlow()

    // ── Error State ─────────────────────────────────────────────────
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadQueue() {
        viewModelScope.launch {
            _queueLoading.value = true
            _error.value = null
            when (val result = api.getModerationQueue()) {
                is ApiResult.Success -> _queueItems.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.NetworkError -> _error.value = "Network error: ${result.throwable.message}"
            }
            _queueLoading.value = false
        }
    }

    fun loadReports() {
        viewModelScope.launch {
            _reportsLoading.value = true
            _error.value = null
            when (val result = api.getReports()) {
                is ApiResult.Success -> _reports.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.NetworkError -> _error.value = "Network error: ${result.throwable.message}"
            }
            _reportsLoading.value = false
        }
    }

    fun loadLicenseRequests() {
        viewModelScope.launch {
            _licenseLoading.value = true
            _error.value = null
            when (val result = api.getLicenseRequests()) {
                is ApiResult.Success -> _licenseRequests.value = result.data.items
                is ApiResult.Error -> _error.value = result.message
                is ApiResult.NetworkError -> _error.value = "Network error: ${result.throwable.message}"
            }
            _licenseLoading.value = false
        }
    }

    fun approveReview(reviewId: String) {
        viewModelScope.launch {
            when (api.decideModerationReview(reviewId, "APPROVED")) {
                is ApiResult.Success -> loadQueue()
                is ApiResult.Error -> _error.value = "Failed to approve"
                is ApiResult.NetworkError -> _error.value = "Network error"
            }
        }
    }

    fun rejectReview(reviewId: String, reason: String? = null, issueStrike: Boolean = false) {
        viewModelScope.launch {
            when (api.decideModerationReview(reviewId, "REJECTED", reason, issueStrike)) {
                is ApiResult.Success -> loadQueue()
                is ApiResult.Error -> _error.value = "Failed to reject"
                is ApiResult.NetworkError -> _error.value = "Network error"
            }
        }
    }

    fun resolveReport(reportId: String, status: String, resolution: String? = null) {
        viewModelScope.launch {
            when (api.resolveReport(reportId, status, resolution)) {
                is ApiResult.Success -> loadReports()
                is ApiResult.Error -> _error.value = "Failed to resolve report"
                is ApiResult.NetworkError -> _error.value = "Network error"
            }
        }
    }

    fun approveLicense(requestId: String) {
        viewModelScope.launch {
            when (api.approveLicenseRequest(requestId)) {
                is ApiResult.Success -> loadLicenseRequests()
                is ApiResult.Error -> _error.value = "Failed to approve license"
                is ApiResult.NetworkError -> _error.value = "Network error"
            }
        }
    }

    fun rejectLicense(requestId: String, reason: String? = null) {
        viewModelScope.launch {
            when (api.rejectLicenseRequest(requestId, reason)) {
                is ApiResult.Success -> loadLicenseRequests()
                is ApiResult.Error -> _error.value = "Failed to reject license"
                is ApiResult.NetworkError -> _error.value = "Network error"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
