package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.search.SaveSearchRequest
import com.cognia.app.service.SearchService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.searchRoutes() {
    val searchService by application.inject<SearchService>()

    route("/api/v1/search") {
        authenticate("auth-jwt") {

            get {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val query = call.request.queryParameters["q"]
                if (query.isNullOrBlank()) {
                    return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Query parameter 'q' is required"))
                }

                val type = call.request.queryParameters["type"]
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = ((page - 1) * limit).toLong()

                val results = searchService.search(query, type, limit, offset)
                call.respond(HttpStatusCode.OK, results)
            }

            get("/history") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val history = searchService.getSearchHistory(principal.userId)
                call.respond(HttpStatusCode.OK, history)
            }

            post("/history") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val request = call.receive<SaveSearchRequest>()
                if (request.query.isBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Query cannot be blank"))
                }

                searchService.saveSearchQuery(principal.userId, request.query)
                call.respond(HttpStatusCode.Created, mapOf("saved" to true))
            }
        }
    }
}
