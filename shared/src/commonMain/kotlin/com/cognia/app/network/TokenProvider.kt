package com.cognia.app.network

interface TokenProvider {
    suspend fun getAccessToken(): String?
    suspend fun getRefreshToken(): String?
    suspend fun refreshTokens(): TokenPair?
    suspend fun clearTokens()
}

data class TokenPair(
    val accessToken: String,
    val refreshToken: String
)
