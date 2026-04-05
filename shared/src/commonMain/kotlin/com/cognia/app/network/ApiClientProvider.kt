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

    fun init(
        baseUrl: String = ApiConfig.baseUrl,
        persistGet: ((String) -> String?)? = null,
        persistSet: ((String, String) -> Unit)? = null,
        persistRemove: ((String) -> Unit)? = null,
    ) {
        if (_client != null) return // Already initialized
        ApiConfig.baseUrl = baseUrl
        val storage = SimpleTokenStorage(
            persistGet = persistGet,
            persistSet = persistSet,
            persistRemove = persistRemove,
        )
        _tokenStorage = storage
        val httpClient = HttpClientFactory.create(storage)
        _client = CogniaApiClient(httpClient)
    }
}

/**
 * Token storage with optional persistence callbacks.
 * On web, pass localStorage get/set/remove lambdas so tokens survive page reloads.
 * On mobile, leave null for in-memory only (mobile apps don't reload).
 */
class SimpleTokenStorage(
    private val persistGet: ((String) -> String?)? = null,
    private val persistSet: ((String, String) -> Unit)? = null,
    private val persistRemove: ((String) -> Unit)? = null,
) : TokenProvider {
    private var accessToken: String? = persistGet?.invoke("cognia_access_token")
    private var refreshToken: String? = persistGet?.invoke("cognia_refresh_token")

    fun setTokens(access: String, refresh: String) {
        accessToken = access
        refreshToken = refresh
        persistSet?.invoke("cognia_access_token", access)
        persistSet?.invoke("cognia_refresh_token", refresh)
    }

    /** Returns true if we have a stored access token (e.g. from a previous session). */
    fun hasStoredTokens(): Boolean = accessToken != null

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
        persistRemove?.invoke("cognia_access_token")
        persistRemove?.invoke("cognia_refresh_token")
    }
}
