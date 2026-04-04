package com.cognia.app.service

import com.cognia.app.database.CategoriesTable
import com.cognia.app.database.VideosTable
import com.cognia.app.dto.ai.GeneratedQuestion
import com.cognia.app.dto.ai.QuizGenerateResponse
import kotlinx.serialization.json.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class AiQuizGenerationService(
    private val anthropicClient: AnthropicRecommendationClient
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val SYSTEM_PROMPT = """
You are a quiz question generator for Cognia, an educational social platform.
Generate multiple-choice quiz questions based on the given topic and context.

Return ONLY a JSON object with:
- "title": a descriptive quiz title
- "questions": array of question objects, each with:
  - "questionText": the question
  - "options": array of 4 answer strings
  - "correctOptionIndex": 0-based index of the correct answer

Ensure questions are:
- Educationally sound and factually correct
- At the specified difficulty level
- Diverse in what they test (recall, comprehension, application)

Example:
{
  "title": "Quantum Physics Basics",
  "questions": [
    {
      "questionText": "What is the Heisenberg Uncertainty Principle?",
      "options": [
        "You cannot know both position and momentum precisely",
        "Energy is always conserved",
        "Light travels in straight lines",
        "Matter cannot be created or destroyed"
      ],
      "correctOptionIndex": 0
    }
  ]
}
        """.trimIndent()
    }

    fun generate(
        title: String,
        description: String?,
        categoryId: String,
        videoId: String?,
        questionCount: Int,
        difficulty: String?
    ): QuizGenerateResponse {
        val categoryName = lookupCategoryName(categoryId) ?: "General"
        val videoContext = if (videoId != null) lookupVideoContext(videoId) else null

        val prompt = buildPrompt(title, description, categoryName, videoContext, questionCount, difficulty)

        val response = anthropicClient.sendMessage(
            prompt = prompt,
            systemPrompt = SYSTEM_PROMPT,
            maxTokens = 2048
        )

        if (response == null) {
            return QuizGenerateResponse(
                title = "$title Quiz",
                questions = emptyList()
            )
        }

        return parseResponse(response, title)
    }

    private fun buildPrompt(
        title: String,
        description: String?,
        categoryName: String,
        videoContext: String?,
        questionCount: Int,
        difficulty: String?
    ): String {
        val parts = mutableListOf<String>()
        parts.add("Topic: $title")
        parts.add("Category: $categoryName")
        if (!description.isNullOrBlank()) parts.add("Description: $description")
        if (videoContext != null) parts.add("Video context: $videoContext")
        parts.add("Number of questions: $questionCount")
        if (!difficulty.isNullOrBlank()) parts.add("Difficulty level: $difficulty")

        return parts.joinToString("\n")
    }

    private fun parseResponse(response: String, fallbackTitle: String): QuizGenerateResponse {
        return try {
            val start = response.indexOf('{')
            val end = response.lastIndexOf('}')
            if (start == -1 || end == -1) return QuizGenerateResponse("$fallbackTitle Quiz", emptyList())

            val jsonStr = response.substring(start, end + 1)
            val obj = json.parseToJsonElement(jsonStr).jsonObject

            val quizTitle = obj["title"]?.jsonPrimitive?.content ?: "$fallbackTitle Quiz"
            val questions = obj["questions"]?.jsonArray?.mapNotNull { qElement ->
                val qObj = qElement.jsonObject
                val questionText = qObj["questionText"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val options = qObj["options"]?.jsonArray?.map { it.jsonPrimitive.content } ?: return@mapNotNull null
                val correctIndex = qObj["correctOptionIndex"]?.jsonPrimitive?.int ?: return@mapNotNull null

                if (options.size < 2 || correctIndex !in options.indices) return@mapNotNull null

                GeneratedQuestion(
                    questionText = questionText,
                    options = options,
                    correctOptionIndex = correctIndex
                )
            } ?: emptyList()

            QuizGenerateResponse(title = quizTitle, questions = questions)
        } catch (e: Exception) {
            QuizGenerateResponse("$fallbackTitle Quiz", emptyList())
        }
    }

    private fun lookupCategoryName(categoryId: String): String? = transaction {
        CategoriesTable.selectAll().where { CategoriesTable.id eq categoryId }
            .singleOrNull()?.get(CategoriesTable.name)
    }

    private fun lookupVideoContext(videoId: String): String? = transaction {
        VideosTable.selectAll().where { VideosTable.id eq videoId }
            .singleOrNull()?.let { row ->
                "${row[VideosTable.title]}: ${row[VideosTable.description] ?: ""}"
            }
    }
}
