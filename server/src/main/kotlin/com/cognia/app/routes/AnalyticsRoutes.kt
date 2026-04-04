package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.service.AnalyticsService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.analyticsRoutes() {
    val analyticsService by application.inject<AnalyticsService>()

    route("/api/v1/analytics") {
        authenticate("auth-jwt") {
            get("/creator") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val analytics = analyticsService.getCreatorAnalytics(principal.userId)
                call.respond(HttpStatusCode.OK, analytics)
            }
        }
    }
}
