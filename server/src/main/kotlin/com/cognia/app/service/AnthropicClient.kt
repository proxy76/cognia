package com.cognia.app.service

import com.cognia.app.config.AppConfig
import kotlinx.serialization.json.*
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

interface RecommendationClient {
    fun getRecommendations(prompt: String): String?
}

/**
 * General-purpose Anthropic Messages API client.
 * Supports system prompts, model selection, and configurable max tokens.
 */
class AnthropicRecommendationClient(
    private val config: AppConfig
) : RecommendationClient {

    private val httpClient: HttpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override fun getRecommendations(prompt: String): String? {
        return sendMessage(prompt = prompt)
    }

    /**
     * Send a message to the Anthropic API with optional system prompt and model override.
     * @param prompt The user message content
     * @param systemPrompt Optional system prompt for context/instructions
     * @param model Override the default model (null = use config default)
     * @param useFastModel If true, use the fast model (haiku) instead of the default
     * @param maxTokens Override max tokens (null = use config default)
     * @return The text response, or null on failure
     */
    fun sendMessage(
        prompt: String,
        systemPrompt: String? = null,
        model: String? = null,
        useFastModel: Boolean = false,
        maxTokens: Int? = null
    ): String? {
        val apiKey = config.ai.anthropicApiKey
        if (apiKey.isBlank()) {
            return null
        }

        val selectedModel = model
            ?: if (useFastModel) config.ai.anthropicFastModel else config.ai.anthropicModel
        val selectedMaxTokens = maxTokens ?: config.ai.maxTokens

        val requestBody = buildJsonObject {
            put("model", selectedModel)
            put("max_tokens", selectedMaxTokens)
            if (systemPrompt != null) {
                put("system", systemPrompt)
            }
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
