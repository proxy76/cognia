package com.cognia.app.service

import com.cognia.app.config.AppConfig
import com.cognia.app.dto.ai.AiModerationResponse
import kotlinx.serialization.json.*

/**
 * AI-powered content moderation pre-screening.
 * Classifies content as: safe, needs_review, or reject.
 */
class AiModerationService(
    private val anthropicClient: AnthropicRecommendationClient,
    private val config: AppConfig
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SYSTEM_PROMPT = """
You are a content moderation system for Cognia, an educational social platform.
Evaluate the content for policy violations:
- Hate speech, harassment, or discrimination
- Sexually explicit or inappropriate content
- Misinformation or dangerous medical/legal advice
- Spam or commercial solicitation
- Copyright infringement indicators
- Violence or graphic content

Return ONLY a JSON object with:
- "assessment": one of "safe", "needs_review", "reject"
- "confidence": 0.0 to 1.0
- "reason": brief explanation

Example: {"assessment": "safe", "confidence": 0.95, "reason": "Educational content about physics with no policy violations."}
        """.trimIndent()
    }

    fun moderate(title: String, description: String?, contentType: String): AiModerationResponse {
        val prompt = buildPrompt(title, description, contentType)

        val response = anthropicClient.sendMessage(
            prompt = prompt,
            systemPrompt = SYSTEM_PROMPT,
            useFastModel = true,
            maxTokens = 256
        )

        if (response == null) {
            return AiModerationResponse(
                assessment = "needs_review",
                confidence = 0.0,
                reason = "AI moderation unavailable, manual review required"
            )
        }

        return parseResponse(response)
    }

    /**
     * Applies threshold-based decision logic.
     * Returns the action to take: "publish", "queue_review", or "reject"
     */
    fun assessAndDecide(title: String, description: String?, contentType: String): Pair<AiModerationResponse, String> {
        val result = moderate(title, description, contentType)
        val action = when {
            result.assessment == "reject" && result.confidence >= config.moderation.autoRejectThreshold -> "reject"
            result.assessment == "reject" || result.assessment == "needs_review" -> "queue_review"
            result.confidence >= config.moderation.reviewThreshold -> "publish"
            else -> "queue_review"
        }
        return result to action
    }

    private fun buildPrompt(title: String, description: String?, contentType: String): String {
        val descPart = if (!description.isNullOrBlank()) "\nDescription: $description" else ""
        return """
Content type: $contentType
Title: $title$descPart
        """.trimIndent()
    }

    private fun parseResponse(response: String): AiModerationResponse {
        return try {
            val start = response.indexOf('{')
            val end = response.lastIndexOf('}')
            if (start == -1 || end == -1) return defaultNeedsReview()

            val jsonStr = response.substring(start, end + 1)
            val obj = json.parseToJsonElement(jsonStr).jsonObject

            AiModerationResponse(
                assessment = obj["assessment"]?.jsonPrimitive?.content ?: "needs_review",
                confidence = obj["confidence"]?.jsonPrimitive?.double ?: 0.0,
                reason = obj["reason"]?.jsonPrimitive?.content ?: "Unable to parse AI response"
            )
        } catch (e: Exception) {
            defaultNeedsReview()
        }
    }

    private fun defaultNeedsReview() = AiModerationResponse(
        assessment = "needs_review",
        confidence = 0.0,
        reason = "Failed to parse AI moderation response"
    )
}
