package com.cognia.app.service

import com.cognia.app.database.CategoriesTable
import com.cognia.app.dto.ai.CategoryMatch
import com.cognia.app.dto.ai.CategorizationResponse
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.repository.CategoryRow
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AiCategorizationService(
    private val anthropicClient: AnthropicRecommendationClient
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SYSTEM_PROMPT = """
You are a content categorization engine for Cognia, an educational social platform.
Given content title and description, match it to the most relevant categories.

Return ONLY a JSON array of objects with "name" (category name, exactly matching the available list) and "confidence" (0.0 to 1.0).
Return the top 3 most relevant categories, ordered by confidence.

Example response:
[{"name": "Physics", "confidence": 0.95}, {"name": "Science", "confidence": 0.82}, {"name": "Mathematics", "confidence": 0.6}]
        """.trimIndent()
    }

    fun categorize(title: String, description: String?): CategorizationResponse {
        val categories = loadCategories()
        if (categories.isEmpty()) return CategorizationResponse(emptyList())

        val categoryNames = categories.map { it.name }
        val prompt = buildPrompt(title, description, categoryNames)

        val response = anthropicClient.sendMessage(
            prompt = prompt,
            systemPrompt = SYSTEM_PROMPT,
            useFastModel = true
        )

        val matches = if (response != null) {
            parseMatches(response, categories)
        } else {
            fallbackMatch(title, description, categories)
        }

        return CategorizationResponse(matches = matches)
    }

    private fun buildPrompt(title: String, description: String?, categoryNames: List<String>): String {
        val descPart = if (!description.isNullOrBlank()) "\nDescription: $description" else ""
        return """
Available categories: ${categoryNames.joinToString(", ")}

Title: $title$descPart
        """.trimIndent()
    }

    private fun parseMatches(response: String, categories: List<CategoryRow>): List<CategoryMatch> {
        return try {
            val start = response.indexOf('[')
            val end = response.lastIndexOf(']')
            if (start == -1 || end == -1) return fallbackMatch("", null, categories)

            val jsonStr = response.substring(start, end + 1)
            val array = json.parseToJsonElement(jsonStr).jsonArray
            val nameLookup = categories.associateBy { it.name.lowercase() }

            array.mapNotNull { element ->
                val obj = element.jsonObject
                val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val confidence = obj["confidence"]?.jsonPrimitive?.double ?: 0.5
                val cat = nameLookup[name.lowercase()] ?: return@mapNotNull null
                CategoryMatch(
                    category = CategoryResponse(id = cat.id, name = cat.name, slug = cat.slug),
                    confidence = confidence
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun fallbackMatch(title: String, description: String?, categories: List<CategoryRow>): List<CategoryMatch> {
        val text = "$title ${description ?: ""}".lowercase()
        val words = text.split(Regex("[\\s,;.!?]+")).filter { it.length > 2 }.toSet()

        return categories
            .map { cat ->
                val nameWords = cat.name.lowercase().split(Regex("[\\s-]+"))
                val matchCount = nameWords.count { it in words }
                cat to matchCount
            }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(3)
            .map { (cat, score) ->
                CategoryMatch(
                    category = CategoryResponse(id = cat.id, name = cat.name, slug = cat.slug),
                    confidence = (score.toDouble() / cat.name.split(" ").size).coerceIn(0.1, 0.9)
                )
            }
    }

    private fun loadCategories(): List<CategoryRow> = transaction {
        CategoriesTable.selectAll().map { row ->
            CategoryRow(
                id = row[CategoriesTable.id],
                name = row[CategoriesTable.name],
                slug = row[CategoriesTable.slug]
            )
        }
    }
}
