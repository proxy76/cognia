package com.cognia.app.service

import com.cognia.app.config.AppConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

interface RecommendationClient {
    fun getRecommendations(prompt: String): String?
}

class AnthropicRecommendationClient(
    private val config: AppConfig
) : RecommendationClient {

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override fun getRecommendations(prompt: String): String? {
        val apiKey = config.ai.anthropicApiKey
        if (apiKey.isBlank()) {
            return null
        }

        val requestBody = buildJsonObject {
            put("model", config.ai.anthropicModel)
            put("max_tokens", 1024)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", prompt)
                }
            }
        }

        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.anthropic.com/v1/messages"))
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

            if (response.statusCode() !in 200..299) {
                return null
            }

            val responseJson = json.parseToJsonElement(response.body()).jsonObject
            val content = responseJson["content"]?.jsonArray ?: return null
            val textBlock = content.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.content == "text" }
            textBlock?.jsonObject?.get("text")?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }
}
