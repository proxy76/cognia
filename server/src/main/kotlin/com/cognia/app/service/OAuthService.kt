package com.cognia.app.service

import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.repository.UserRepository

class OAuthService(
    private val userRepository: UserRepository,
    private val authService: AuthService
) {

    fun authenticateWithGoogle(idToken: String, googleTokenVerifier: GoogleTokenVerifier): AuthResponse {
        val payload = googleTokenVerifier.verify(idToken)
            ?: throw OAuthVerificationException("Google token verification failed")

        if (!payload.emailVerified) {
            throw OAuthVerificationException("Google email not verified")
        }

        return findOrCreateUser(payload.email, payload.name, "GOOGLE")
    }

    fun authenticateWithApple(
        identityToken: String,
        authorizationCode: String,
        appleTokenVerifier: AppleTokenVerifier
    ): AuthResponse {
        val payload = appleTokenVerifier.verify(identityToken, authorizationCode)
            ?: throw OAuthVerificationException("Apple token verification failed")

        return findOrCreateUser(payload.email, payload.name, "APPLE")
    }

    private fun findOrCreateUser(email: String, name: String?, authProvider: String): AuthResponse {
        val existingUser = userRepository.findByEmail(email)

        val user = if (existingUser != null) {
            existingUser
        } else {
            val displayName = name ?: email.substringBefore("@")
            userRepository.createOAuthUser(email, displayName, authProvider)
        }

        return authService.generateAuthResponse(user.id, user.email, user.role)
    }
}

class OAuthVerificationException(message: String) : RuntimeException(message)
