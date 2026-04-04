package com.cognia.app.ui.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class AuthViewModelTest {

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
    fun registerWithValidDataSetsSuccess() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John Doe")
        viewModel.register()

        val success = viewModel.state.value.authSuccess
        assertNotNull(success)
        assertTrue(success.isNewUser)
        assertFalse(viewModel.state.value.isLoading)
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
    fun loginWithValidDataSetsSuccess() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.login()

        val success = viewModel.state.value.authSuccess
        assertNotNull(success)
        assertFalse(success.isNewUser)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun clearStateResetsToInitial() {
        val viewModel = AuthViewModel()
        viewModel.updateEmail("test@example.com")
        viewModel.updatePassword("password123")
        viewModel.updateDisplayName("John")
        viewModel.register()

        viewModel.clearState()
        val state = viewModel.state.value

        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.displayName)
        assertNull(state.authSuccess)
        assertFalse(state.isLoading)
    }
}
