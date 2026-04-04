package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.ai.*
import com.cognia.app.service.AiCategorizationService
import com.cognia.app.service.AiModerationService
import com.cognia.app.service.AiQuizGenerationService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.aiRoutes() {
    val categorizationService by application.inject<AiCategorizationService>()
    val moderationService by application.inject<AiModerationService>()
    val quizGenerationService by application.inject<AiQuizGenerationService>()

    route("/api/v1/ai") {
        authenticate("auth-jwt") {

            // POST /categorize — AI-powered category matching
            post("/categorize") {
                val request = call.receive<CategorizationRequest>()

                if (request.title.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Title is required"))
                }

                val result = categorizationService.categorize(request.title, request.description)
                call.respond(HttpStatusCode.OK, result)
            }

            // POST /moderate — AI content pre-screening
            post("/moderate") {
                val request = call.receive<AiModerationRequest>()

                if (request.contentType !in setOf("VIDEO", "QUIZ")) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Content type must be VIDEO or QUIZ"))
                }

                val (result, action) = moderationService.assessAndDecide(
                    title = request.title,
                    description = request.description,
                    contentType = request.contentType
                )

                call.respond(HttpStatusCode.OK, result)
            }

            // POST /quiz-generate — AI quiz question generation
            post("/quiz-generate") {
                val request = call.receive<QuizGenerateRequest>()

                if (request.title.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Title is required"))
                }
                if (request.categoryId.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Category ID is required"))
                }

                val questionCount = request.questionCount.coerceIn(1, 20)

                val result = quizGenerationService.generate(
                    title = request.title,
                    description = request.description,
                    categoryId = request.categoryId,
                    videoId = request.videoId,
                    questionCount = questionCount,
                    difficulty = request.difficulty
                )

                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
