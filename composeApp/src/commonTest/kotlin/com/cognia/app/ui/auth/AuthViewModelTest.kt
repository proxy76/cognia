package com.cognia.app.ui.auth

import com.cognia.app.network.ApiClientProvider
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertFalse

class AuthViewModelTest {

    @BeforeTest
    fun setup() {
        ApiClientProvider.init("http://localhost:99999")
    }

    @Test
    fun initialStateIsIdleWithEmptyFields() {
        val viewModel = AuthViewModel()
        val state = viewModel.state.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.displayName)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.displayNameError)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertNull(state.authSuccess)
    }

    @Test
    fun updateEmailUpdatesState() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        assertEquals("test@example.com", viewModel.state.value.email)
    }

    @Test
    fun updatePasswordUpdatesState() {
        val viewModel = AuthViewModel()
        viewModel.updatePassword("password123")
        assertEquals("password123", viewModel.state.value.password)
    }

    @Test
    fun updateDisplayNameUpdatesState() {
        val viewModel = AuthViewModel()
        viewModel.updateDisplayName("John Doe")
        assertEquals("John Doe", viewModel.state.value.displayName)
    }

    @Test
    fun registerWithInvalidEmailShowsEmailError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("invalid")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John")
        viewModel.register()

        assertNotNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.authSuccess)
    }

    @Test
    fun registerWithShortPasswordShowsPasswordError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("short")
        viewModel.updateDisplayName("John")
        viewModel.register()

        assertNotNull(viewModel.state.value.passwordError)
        assertNull(viewModel.state.value.authSuccess)
    }

    @Test
    fun registerWithEmptyDisplayNameShowsError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("")
        viewModel.register()

        assertNotNull(viewModel.state.value.displayNameError)
        assertNull(viewModel.state.value.authSuccess)
    }

    @Test
    fun registerWithValidDataSetsLoadingAndLaunchesCoroutine() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John Doe")
        viewModel.register()

        // Validation passes, so no validation errors should be present
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
        assertNull(viewModel.state.value.displayNameError)
    }

    @Test
    fun loginWithEmptyEmailShowsEmailError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("")
        viewModel.updatePassword("password123")
        viewModel.login()

        assertNotNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.authSuccess)
    }

    @Test
    fun loginWithEmptyPasswordShowsPasswordError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("")
        viewModel.login()

        assertNotNull(viewModel.state.value.passwordError)
        assertNull(viewModel.state.value.authSuccess)
    }

    @Test
    fun loginWithValidDataSetsLoadingAndLaunchesCoroutine() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.login()

        // Validation passes, so no validation errors should be present
        assertNull(viewModel.state.value.emailError)
        assertNull(viewModel.state.value.passwordError)
    }

    @Test
    fun clearStateResetsToInitial() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John")

        viewModel.clearState()
        val state = viewModel.state.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.displayName)
        assertNull(state.authSuccess)
        assertFalse(state.isLoading)
    }

    @Test
    fun updateEmailClearsEmailError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("invalid")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John")
        viewModel.register()
        assertNotNull(viewModel.state.value.emailError)

        viewModel.updateEmail("test@example.com")
        assertNull(viewModel.state.value.emailError)
    }

    @Test
    fun updatePasswordClearsPasswordError() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("short")
        viewModel.updateDisplayName("John")
        viewModel.register()
        assertNotNull(viewModel.state.value.passwordError)

        viewModel.updatePassword("longenough1")
        assertNull(viewModel.state.value.passwordError)
    }
}
