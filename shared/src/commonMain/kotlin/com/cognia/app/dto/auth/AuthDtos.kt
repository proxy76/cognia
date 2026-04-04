package com.cognia.app.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class GoogleOAuthRequest(val idToken: String)

@Serializable
data class AppleOAuthRequest(
    val identityToken: String,
    val authorizationCode: String
)

@Serializable
data class RefreshTokenRequest(val refreshToken: String)

@Serializable
data class AuthResponse(
    val userId: String,
    val token: String,
    val refreshToken: String
)
