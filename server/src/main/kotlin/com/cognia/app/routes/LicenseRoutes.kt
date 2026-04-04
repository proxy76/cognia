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

fun Route.licenseRoutes() {
    val licenseService by application.inject<LicenseService>()

    route("/api/v1/license") {
        authenticate("auth-jwt") {

            // POST /request — request a license (REGULAR_CREATOR)
            authorize("REGULAR_CREATOR") {
                post("/request") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    try {
                        val result = licenseService.requestLicense(principal.userId, principal.role)
                        call.respond(HttpStatusCode.Created, result)
                    } catch (e: LicenseRequestDeniedException) {
                        call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Request denied"))
                    }
                }
            }

            // GET /requests — list requests (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                get("/requests") {
                    val requests = licenseService.getRequests()
                    call.respond(HttpStatusCode.OK, LicenseRequestListResponse(requests))
                }
            }

            // POST /{id}/approve — approve (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                post("/{id}/approve") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing request ID"))

                    try {
                        val result = licenseService.approveRequest(id, principal.userId)
                        call.respond(HttpStatusCode.OK, result)
                    } catch (e: LicenseRequestNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Request not found"))
                    } catch (e: LicenseRequestDeniedException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Cannot approve"))
                    }
                }
            }

            // POST /{id}/reject — reject (MODERATOR, ADMIN)
            authorize("MODERATOR", "ADMIN") {
                post("/{id}/reject") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing request ID"))

                    try {
                        val request = call.receive<LicenseRejectRequest>()
                        val result = licenseService.rejectRequest(id, principal.userId, request.reason)
                        call.respond(HttpStatusCode.OK, result)
                    } catch (e: LicenseRequestNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Request not found"))
                    } catch (e: LicenseRequestDeniedException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Cannot reject"))
                    }
                }
            }
        }
    }
}
