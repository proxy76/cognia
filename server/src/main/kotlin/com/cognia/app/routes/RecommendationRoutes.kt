package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.onboarding.RecommendRequest
import com.cognia.app.dto.onboarding.RecommendResponse
import com.cognia.app.service.OnboardingService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.recommendationRoutes() {
    val onboardingService by application.inject<OnboardingService>()

    route("/api/v1/onboarding") {
        authenticate("auth-jwt") {
            post("/recommend") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                try {
                    val request = call.receive<RecommendRequest>()
                    if (request.selfDescription.isBlank()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorBody("selfDescription must not be blank")
                        )
                    }
                    val categories = onboardingService.recommend(
                        selfDescription = request.selfDescription,
                        answers = request.answers
                    )
                    call.respond(HttpStatusCode.OK, RecommendResponse(recommendedCategories = categories))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorBody("Failed to generate recommendations")
                    )
                }
            }
        }
    }
}
