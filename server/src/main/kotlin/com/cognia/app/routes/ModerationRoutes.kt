package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.auth.authorize
import com.cognia.app.dto.moderation.*
import com.cognia.app.service.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.moderationRoutes() {
    val moderationService by application.inject<ModerationService>()
    val reportService by application.inject<ReportService>()
    val strikeService by application.inject<StrikeService>()

    route("/api/v1/moderation") {
        authenticate("auth-jwt") {

            // GET /queue — pending reviews (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                get("/queue") {
                    val items = moderationService.getPendingQueue()
                    call.respond(HttpStatusCode.OK, ModerationQueueResponse(items))
                }
            }

            // POST /{id}/decide — approve or reject (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                post("/{id}/decide") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing review ID"))

                    try {
                        val request = call.receive<ModerationDecisionRequest>()

                        if (request.decision !in setOf("APPROVED", "REJECTED")) {
                            return@post call.respond(
                                HttpStatusCode.BadRequest,
                                ErrorBody("Decision must be APPROVED or REJECTED")
                            )
                        }

                        val result = moderationService.decide(id, principal.userId, request.decision, request.reason)

                        // If rejected and issueStrike is true, issue a strike to the content creator
                        if (request.decision == "REJECTED" && request.issueStrike) {
                            val review = moderationService.getReviewById(id)
                            if (review != null) {
                                val creatorId = moderationService.getContentCreatorId(review.contentId, review.contentType)
                                if (creatorId != null) {
                                    strikeService.issueStrike(
                                        userId = creatorId,
                                        moderatorId = principal.userId,
                                        reason = request.reason ?: "Content rejected"
                                    )
                                }
                            }
                        }

                        call.respond(HttpStatusCode.OK, result)
                    } catch (e: ModerationNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Review not found"))
                    } catch (e: ModerationAlreadyDecidedException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Already decided"))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }
            }

            // GET /reviews — review history (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                get("/reviews") {
                    val reviews = moderationService.getReviewHistory()
                    call.respond(HttpStatusCode.OK, ModerationReviewListResponse(reviews))
                }
            }
        }
    }

    route("/api/v1/reports") {
        authenticate("auth-jwt") {

            // POST / — report content (any authenticated user)
            post {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                try {
                    val request = call.receive<ReportCreateRequest>()
                    val report = reportService.reportContent(
                        reporterId = principal.userId,
                        contentId = request.contentId,
                        contentType = request.contentType,
                        reason = request.reason
                    )
                    call.respond(HttpStatusCode.Created, report)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            // GET / — list reports (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                get {
                    val reports = reportService.getReports()
                    call.respond(HttpStatusCode.OK, ReportListResponse(reports))
                }
            }
        }
    }
}
