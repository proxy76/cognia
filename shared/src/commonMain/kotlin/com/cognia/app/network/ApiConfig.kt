package com.cognia.app.network

object ApiConfig {
    var baseUrl: String = "http://localhost:8080"
    const val API_PREFIX = "/api/v1"

    fun apiUrl(path: String): String = "$baseUrl$API_PREFIX$path"
}
