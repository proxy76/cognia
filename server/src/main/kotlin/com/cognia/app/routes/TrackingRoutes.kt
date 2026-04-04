package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.feed.TrackShareRequest
import com.cognia.app.dto.feed.TrackViewRequest
import com.cognia.app.dto.feed.TrackingResponse
import com.cognia.app.service.TrackingService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.trackingRoutes() {
    val trackingService by application.inject<TrackingService>()

    route("/api/v1/tracking") {
        authenticate("auth-jwt") {

            post("/view") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val request = call.receive<TrackViewRequest>()
                trackingService.trackView(principal.userId, request.contentId, request.contentType)
                call.respond(HttpStatusCode.OK, TrackingResponse(success = true))
            }

            post("/share") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val request = call.receive<TrackShareRequest>()
                trackingService.trackShare(principal.userId, request.contentId, request.contentType)
                call.respond(HttpStatusCode.OK, TrackingResponse(success = true))
            }
        }
    }
}
