package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.auth.authorize
import com.cognia.app.dto.quiz.CreateQuizRequest
import com.cognia.app.dto.quiz.QuizAttemptRequest
import com.cognia.app.service.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.quizRoutes() {
    val quizService by application.inject<QuizService>()
    val scoringService by application.inject<ScoringService>()

    route("/api/v1/quizzes") {
        authenticate("auth-jwt") {

            // POST / — create quiz (creators only)
            authorize("REGULAR_CREATOR", "LICENSED_CREATOR") {
                post {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    try {
                        val request = call.receive<CreateQuizRequest>()
                        val quiz = quizService.createQuiz(principal.userId, request)
                        call.respond(HttpStatusCode.Created, quiz)
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }
            }

            // GET /{id} — get quiz (hides answers for non-owner)
            get("/{id}") {
                val principal = call.principal<UserPrincipal>()
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing quiz ID"))

                try {
                    val quiz = quizService.getQuiz(id, principal?.userId)
                    call.respond(HttpStatusCode.OK, quiz)
                } catch (e: QuizNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Quiz not found"))
                }
            }

            // GET /my — get current user's quizzes
            get("/my") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val quizzes = quizService.getCreatorQuizzes(principal.userId)
                call.respond(HttpStatusCode.OK, quizzes)
            }

            // POST /{id}/submit — submit for review
            post("/{id}/submit") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val id = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing quiz ID"))

                try {
                    val quiz = quizService.submitForReview(id, principal.userId)
                    call.respond(HttpStatusCode.OK, quiz)
                } catch (e: QuizNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Quiz not found"))
                } catch (e: QuizAccessDeniedException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                } catch (e: InvalidStateTransitionException) {
                    call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Invalid state transition"))
                }
            }

            // POST /{id}/publish — direct publish (licensed creators only)
            authorize("LICENSED_CREATOR") {
                post("/{id}/publish") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing quiz ID"))

                    try {
                        val quiz = quizService.publishDirect(id, principal.userId)
                        call.respond(HttpStatusCode.OK, quiz)
                    } catch (e: QuizNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Quiz not found"))
                    } catch (e: QuizAccessDeniedException) {
                        call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                    } catch (e: InvalidStateTransitionException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Invalid state transition"))
                    }
                }
            }

            // POST /{id}/attempt — submit quiz attempt
            post("/{id}/attempt") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val id = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing quiz ID"))

                try {
                    val request = call.receive<QuizAttemptRequest>()
                    val result = scoringService.scoreAttempt(id, principal.userId, request.answers)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: QuizNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Quiz not found"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }
        }
    }
}
