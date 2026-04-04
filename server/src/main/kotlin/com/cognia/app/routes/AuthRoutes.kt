package com.cognia.app.routes

import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.service.AuthService
import com.cognia.app.service.EmailAlreadyExistsException
import com.cognia.app.service.InvalidCredentialsException
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class ErrorBody(val error: String)

fun Route.authRoutes() {
    val authService by application.inject<AuthService>()

    route("/api/v1/auth") {
        post("/register") {
            try {
                val request = call.receive<RegisterRequest>()
                val response = authService.register(request)
                call.respond(HttpStatusCode.Created, response)
            } catch (e: EmailAlreadyExistsException) {
                call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Email already in use"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
            }
        }

        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val response = authService.login(request)
                call.respond(HttpStatusCode.OK, response)
            } catch (e: InvalidCredentialsException) {
                call.respond(HttpStatusCode.Unauthorized, ErrorBody(e.message ?: "Invalid credentials"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
            }
        }
    }
}
