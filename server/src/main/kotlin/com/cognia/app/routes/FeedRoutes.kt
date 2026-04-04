package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.service.FeedService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.feedRoutes() {
    val feedService by application.inject<FeedService>()

    route("/api/v1/feed") {
        authenticate("auth-jwt") {

            get("/foryou") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                val feed = feedService.getForYouFeed(principal.userId, page, limit)
                call.respond(HttpStatusCode.OK, feed)
            }

            get("/deepdive") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                val feed = feedService.getDeepDiveFeed(principal.userId, page, limit)
                call.respond(HttpStatusCode.OK, feed)
            }
        }
    }
}
