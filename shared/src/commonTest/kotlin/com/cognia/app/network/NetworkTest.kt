package com.cognia.app.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NetworkTest {

    @Test
    fun apiConfigReturnsCorrectUrl() {
        val originalBase = ApiConfig.baseUrl
        ApiConfig.baseUrl = "http://localhost:8080"
        val url = ApiConfig.apiUrl("/users")
        assertEquals("http://localhost:8080/api/v1/users", url)
        ApiConfig.baseUrl = originalBase
    }

    @Test
    fun apiConfigBaseUrlCanBeChanged() {
        val originalBase = ApiConfig.baseUrl
        ApiConfig.baseUrl = "https://api.cognia.app"
        val url = ApiConfig.apiUrl("/courses")
        assertEquals("https://api.cognia.app/api/v1/courses", url)
        ApiConfig.baseUrl = originalBase
    }

    @Test
    fun httpClientFactoryCreatesClientWithoutTokenProvider() {
        val client = HttpClientFactory.create()
        assertNotNull(client)
        client.close()
    }
}
