package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.profile.UpdateProfileRequest
import com.cognia.app.service.UserNotFoundException
import com.cognia.app.service.UserProfileService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.profileRoutes() {
    val userProfileService by application.inject<UserProfileService>()

    route("/api/v1/users") {
        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                try {
                    val profile = userProfileService.getMyProfile(principal.userId)
                    call.respond(HttpStatusCode.OK, profile)
                } catch (e: UserNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "User not found"))
                }
            }

            put("/me") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                try {
                    val request = call.receive<UpdateProfileRequest>()
                    val profile = userProfileService.updateMyProfile(principal.userId, request)
                    call.respond(HttpStatusCode.OK, profile)
                } catch (e: UserNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "User not found"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            get("/{userId}") {
                val userId = call.parameters["userId"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing userId"))
                val profile = userProfileService.getPublicProfile(userId)
                if (profile != null) {
                    call.respond(HttpStatusCode.OK, profile)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorBody("User not found"))
                }
            }
        }
    }
}
