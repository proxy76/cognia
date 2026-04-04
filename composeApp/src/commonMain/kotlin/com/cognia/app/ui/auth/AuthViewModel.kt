package com.cognia.app.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val displayName: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val displayNameError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val authSuccess: AuthSuccessData? = null
)

data class AuthSuccessData(
    val userId: String,
    val token: String,
    val isNewUser: Boolean
)

class AuthViewModel : ViewModel() {
    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun updateEmail(email: String) {
        _state.value = _state.value.copy(email = email, emailError = null, error = null)
    }

    fun updatePassword(password: String) {
        _state.value = _state.value.copy(password = password, passwordError = null, error = null)
    }

    fun updateDisplayName(name: String) {
        _state.value = _state.value.copy(displayName = name, displayNameError = null, error = null)
    }

    fun register() {
        val current = _state.value

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null
        var displayNameError: String? = null

        if (current.email.isBlank() || !current.email.contains("@") || !current.email.contains(".")) {
            emailError = "Please enter a valid email address"
            hasError = true
        }
        if (current.password.length < 8) {
            passwordError = "Password must be at least 8 characters"
            hasError = true
        }
        if (current.displayName.isBlank()) {
            displayNameError = "Display name is required"
            hasError = true
        }

        if (hasError) {
            _state.value = current.copy(
                emailError = emailError,
                passwordError = passwordError,
                displayNameError = displayNameError
            )
            return
        }

        _state.value = current.copy(isLoading = true, error = null)

        // TODO: Wire to actual API call via repository/use case
        // For now, simulate success after validation passes
        _state.value = _state.value.copy(
            isLoading = false,
            authSuccess = AuthSuccessData(
                userId = "pending",
                token = "pending",
                isNewUser = true
            )
        )
    }

    fun login() {
        val current = _state.value

        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (current.email.isBlank() || !current.email.contains("@")) {
            emailError = "Please enter a valid email address"
            hasError = true
        }
        if (current.password.isBlank()) {
            passwordError = "Password is required"
            hasError = true
        }

        if (hasError) {
            _state.value = current.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        _state.value = current.copy(isLoading = true, error = null)

        // TODO: Wire to actual API call
        _state.value = _state.value.copy(
            isLoading = false,
            authSuccess = AuthSuccessData(
                userId = "pending",
                token = "pending",
                isNewUser = false
            )
        )
    }

    fun clearState() {
        _state.value = AuthUiState()
    }
}
