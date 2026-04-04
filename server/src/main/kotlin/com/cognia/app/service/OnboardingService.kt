package com.cognia.app.service

import com.cognia.app.database.CategoriesTable
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.onboarding.OnboardingAnswer
import com.cognia.app.repository.CategoryRow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class OnboardingService(
    private val recommendationClient: RecommendationClient
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun recommend(
        selfDescription: String,
        answers: List<OnboardingAnswer>
    ): List<CategoryResponse> {
        val availableCategories = loadCategories()
        if (availableCategories.isEmpty()) {
            return emptyList()
        }

        val categoryNames = availableCategories.map { it.name }
        val answerTexts = answers.map { it.answer }

        val prompt = buildPrompt(selfDescription, answerTexts, categoryNames)
        val aiResponse = recommendationClient.getRecommendations(prompt)

        val recommendedNames = if (aiResponse != null) {
            parseRecommendedNames(aiResponse)
        } else {
            null
        }

        val matchedCategories = if (!recommendedNames.isNullOrEmpty()) {
            matchByNames(recommendedNames, availableCategories)
        } else {
            fallbackKeywordMatch(selfDescription, answerTexts, availableCategories)
        }

        return matchedCategories.map { cat ->
            CategoryResponse(id = cat.id, name = cat.name, slug = cat.slug)
        }
    }

    private fun buildPrompt(
        selfDescription: String,
        answers: List<String>,
        categoryNames: List<String>
    ): String {
        val categoriesList = categoryNames.joinToString(", ")
        val answersSection = if (answers.isNotEmpty()) {
            "\nTheir interests: ${answers.joinToString(", ")}"
        } else {
            ""
        }

        return """
You are helping a user find learning categories on Cognia, an educational social platform.

Available categories: [$categoriesList]

The user described themselves as: "$selfDescription"$answersSection

Based on this information, recommend 3-8 categories from the available list that would be most relevant. Return ONLY a JSON array of category names, e.g. ["Science", "Mathematics", "Physics"]. Do not include any other text.
        """.trimIndent()
    }

    private fun parseRecommendedNames(response: String): List<String>? {
        return try {
            // The response might have markdown wrapping, extract the JSON array
            val jsonStr = extractJsonArray(response)
            val array = json.parseToJsonElement(jsonStr).jsonArray
            array.map { it.jsonPrimitive.content }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonArray(text: String): String {
        // Find the first '[' and last ']' to extract the JSON array
        val start = text.indexOf('[')
        val end = text.lastIndexOf(']')
        if (start == -1 || end == -1 || end <= start) {
            throw IllegalArgumentException("No JSON array found in response")
        }
        return text.substring(start, end + 1)
    }

    private fun matchByNames(
        names: List<String>,
        categories: List<CategoryRow>
    ): List<CategoryRow> {
        val nameLookup = categories.associateBy { it.name.lowercase() }
        return names.mapNotNull { name ->
            nameLookup[name.lowercase()]
        }.distinct()
    }

    internal fun fallbackKeywordMatch(
        selfDescription: String,
        answers: List<String>,
        categories: List<CategoryRow>
    ): List<CategoryRow> {
        val allText = (selfDescription + " " + answers.joinToString(" ")).lowercase()
        val words = allText.split(Regex("[\\s,;.!?]+")).filter { it.length > 2 }.toSet()

        val scored = categories.map { cat ->
            val nameWords = cat.name.lowercase().split(Regex("[\\s-]+"))
            val matchCount = nameWords.count { it in words }
            cat to matchCount
        }

        val matched = scored.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return if (matched.isNotEmpty()) {
            matched.take(8)
        } else {
            // Return first few categories as defaults
            categories.take(5)
        }
    }

    private fun loadCategories(): List<CategoryRow> {
        return transaction {
            CategoriesTable.selectAll().map { row ->
                CategoryRow(
                    id = row[CategoriesTable.id],
                    name = row[CategoriesTable.name],
                    slug = row[CategoriesTable.slug]
                )
            }
        }
    }
}
