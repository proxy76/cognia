package com.cognia.app.network

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun refreshTokens(): TokenPair?
    suspend fun clearTokens()

    /** Synchronous access to the current token (for non-suspend contexts like defaultRequest). */
    fun currentAccessToken(): String?
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
