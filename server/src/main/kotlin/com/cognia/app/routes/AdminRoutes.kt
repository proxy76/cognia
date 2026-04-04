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

fun Route.adminRoutes() {
    val adminService by application.inject<AdminService>()
    val auditLogService by application.inject<AuditLogService>()
    val strikeService by application.inject<StrikeService>()
    val reportService by application.inject<ReportService>()

    route("/api/v1") {
        authenticate("auth-jwt") {

            // ── Report Resolution ──────────────────────────────────────

            authorize("MODERATOR", "ADMIN") {
                // PATCH /reports/{id}/resolve — resolve a report
                patch("/reports/{id}/resolve") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@patch call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@patch call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing report ID"))

                    try {
                        val request = call.receive<ReportResolveRequest>()
                        val result = reportService.resolveReport(id, request.status, request.resolution)

                        auditLogService.log(
                            moderatorId = principal.userId,
                            action = "RESOLVE_REPORT",
                            targetType = "REPORT",
                            targetId = id,
                            details = "Status: ${request.status}, Resolution: ${request.resolution ?: "none"}"
                        )

                        call.respond(HttpStatusCode.OK, result)
                    } catch (e: ReportNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Report not found"))
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }

                // GET /reports/stats — report statistics
                get("/reports/stats") {
                    val stats = reportService.getReportStats()
                    call.respond(HttpStatusCode.OK, stats)
                }

                // GET /strikes — strike history for a user
                get("/strikes") {
                    val userId = call.parameters["userId"]
                        ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing userId query param"))

                    val strikes = strikeService.getStrikesByUserId(userId)
                    call.respond(HttpStatusCode.OK, strikes)
                }
            }

            // ── Admin-only endpoints ───────────────────────────────────

            authorize("ADMIN") {
                // POST /users/{id}/suspend — suspend a user
                post("/users/{id}/suspend") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val userId = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user ID"))

                    try {
                        val request = call.receive<SuspendUserRequest>()
                        adminService.suspendUser(userId, principal.userId, request.durationDays, request.reason)
                        call.respond(HttpStatusCode.OK, mapOf("message" to "User suspended", "userId" to userId))
                    } catch (e: UserNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "User not found"))
                    }
                }

                // DELETE /users/{id}/suspend — unsuspend a user
                delete("/users/{id}/suspend") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val userId = call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user ID"))

                    adminService.unsuspendUser(userId, principal.userId)
                    call.respond(HttpStatusCode.OK, mapOf("message" to "User unsuspended", "userId" to userId))
                }

                // GET /moderation/audit — moderation audit log
                get("/moderation/audit") {
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 100
                    val offset = call.parameters["offset"]?.toLongOrNull() ?: 0
                    val entries = auditLogService.getEntries(limit.coerceIn(1, 500), offset)
                    call.respond(HttpStatusCode.OK, AuditLogResponse(entries = entries))
                }
            }
        }
    }
}
