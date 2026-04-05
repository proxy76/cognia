package com.cognia.app.plugins

import com.cognia.app.service.EmailAlreadyExistsException
import com.cognia.app.service.InvalidCredentialsException
import com.cognia.app.service.InvalidRefreshTokenException
import com.cognia.app.service.OAuthVerificationException
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
        // ── Auth exceptions ─────────────────────────────────────────
        exception<InvalidCredentialsException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.Unauthorized,
                "INVALID_CREDENTIALS",
                cause.message ?: "Invalid email or password"
            )
        }
        exception<EmailAlreadyExistsException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.Conflict,
                "EMAIL_EXISTS",
                cause.message ?: "Email already in use"
            )
        }
        exception<InvalidRefreshTokenException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.Unauthorized,
                "INVALID_REFRESH_TOKEN",
                cause.message ?: "Invalid or expired refresh token"
            )
        }
        exception<OAuthVerificationException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.Unauthorized,
                "OAUTH_VERIFICATION_FAILED",
                cause.message ?: "Token verification failed"
            )
        }

        // ── Validation errors ───────────────────────────────────────
        exception<IllegalArgumentException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.BadRequest,
                "BAD_REQUEST",
                cause.message ?: "Invalid request"
            )
        }

        // Request body deserialization failures
        exception<io.ktor.server.plugins.BadRequestException> { call, cause ->
            call.respondJsonError(
                HttpStatusCode.BadRequest,
                "BAD_REQUEST",
                cause.message ?: "Malformed request body"
            )
        }

        // ── Fallback for unmatched routes ───────────────────────────
        status(HttpStatusCode.NotFound) { call, status ->
            call.respondJsonError(status, "NOT_FOUND", "Resource not found")
        }

        // ── Generic fallback ────────────────────────────────────────
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
