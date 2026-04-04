package com.cognia.app.network

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(tokenProvider: TokenProvider? = null): HttpClient {
        return HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = false
                    isLenient = false
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }

            if (tokenProvider != null) {
                install(Auth) {
                    bearer {
                        loadTokens {
                            val token = tokenProvider.getAccessToken()
                            val refresh = tokenProvider.getRefreshToken()
                            if (token != null) BearerTokens(token, refresh ?: "") else null
                        }
                        refreshTokens {
                            val newTokens = tokenProvider.refreshTokens()
                            if (newTokens != null) BearerTokens(newTokens.accessToken, newTokens.refreshToken) else null
                        }
                    }
                }
            }

            defaultRequest {
                url(ApiConfig.baseUrl)
            }
        }
    }
}
