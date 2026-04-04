package com.cognia.app.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class ErrorDetail(val code: String, val message: String)

@Serializable
data class ErrorResponse(val error: ErrorDetail)

private val errorJson = Json {
    prettyPrint = true
    encodeDefaults = true
}

fun Application.configureStatusPages() {
    install(StatusPages) {
        status(HttpStatusCode.BadRequest) { call, status ->
            call.respondJsonError(status, "BAD_REQUEST", "Bad request")
        }
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respondJsonError(status, "UNAUTHORIZED", "Authentication required")
        }
        status(HttpStatusCode.Forbidden) { call, status ->
            call.respondJsonError(status, "FORBIDDEN", "Access denied")
        }
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondJsonError(status, "NOT_FOUND", "Resource not found")
        }
        status(HttpStatusCode.Conflict) { call, status ->
            call.respondJsonError(status, "CONFLICT", "Resource conflict")
        }
        status(HttpStatusCode.InternalServerError) { call, status ->
            call.respondJsonError(status, "INTERNAL_SERVER_ERROR", "Internal server error")
        }
        exception<Throwable> { call, cause ->
            call.application.environment.log.error("Unhandled exception", cause)
            call.respondJsonError(
                HttpStatusCode.InternalServerError,
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred"
            )
        }
    }
}

private suspend fun ApplicationCall.respondJsonError(
    status: HttpStatusCode,
    code: String,
    message: String,
) {
    val body = errorJson.encodeToString(ErrorResponse(ErrorDetail(code, message)))
    respondText(body, ContentType.Application.Json, status)
}
