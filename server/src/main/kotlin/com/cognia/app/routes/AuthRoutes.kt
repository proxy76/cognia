package com.cognia.app.routes

import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RefreshTokenRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.service.AuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authService by application.inject<AuthService>()

    route("/api/v1/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val response = authService.register(request)
            call.respond(HttpStatusCode.Created, response)
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            val response = authService.login(request)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val response = authService.refresh(request)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
