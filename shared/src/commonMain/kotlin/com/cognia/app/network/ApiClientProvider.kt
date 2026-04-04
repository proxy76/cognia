package com.cognia.app.network

/**
 * Singleton holder for the shared API client instance.
 * Initialized once at app startup with optional token provider.
 */
object ApiClientProvider {
    private var _client: CogniaApiClient? = null

    val client: CogniaApiClient
        get() = _client ?: error("ApiClientProvider not initialized. Call init() at app startup.")

    private var _tokenStorage: SimpleTokenStorage? = null

    val tokenStorage: SimpleTokenStorage
        get() = _tokenStorage ?: error("ApiClientProvider not initialized. Call init() at app startup.")

    fun init(baseUrl: String = ApiConfig.baseUrl) {
        if (_client != null) return // Already initialized
        ApiConfig.baseUrl = baseUrl
        val storage = SimpleTokenStorage()
        _tokenStorage = storage
        val httpClient = HttpClientFactory.create(storage)
        _client = CogniaApiClient(httpClient)
    }
}

/**
 * Simple in-memory token storage implementing TokenProvider.
 * In a real app, this would persist to platform-specific secure storage.
 */
class SimpleTokenStorage : TokenProvider {
    private var accessToken: String? = null
    private var refreshToken: String? = null

    fun setTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
    }

    override suspend fun getAccessToken(): String? = accessToken
    override suspend fun getRefreshToken(): String? = refreshToken

    override suspend fun refreshTokens(): TokenPair? {
        // Token refresh would go through the API client
        // For now, return null to force re-login
        return null
    }

    override suspend fun clearTokens() {
        accessToken = null
        refreshToken = null
    }
}
