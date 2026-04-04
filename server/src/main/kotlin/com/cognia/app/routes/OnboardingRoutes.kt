package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.onboarding.SavePreferencesRequest
import com.cognia.app.service.InvalidCategoryIdsException
import com.cognia.app.service.PreferenceService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.onboardingRoutes() {
    val preferenceService by application.inject<PreferenceService>()

    route("/api/v1/onboarding") {
        authenticate("auth-jwt") {
            get("/preferences") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val preferences = preferenceService.getPreferences(principal.userId)
                call.respond(HttpStatusCode.OK, preferences)
            }

            put("/preferences") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                try {
                    val request = call.receive<SavePreferencesRequest>()
                    val preferences = preferenceService.savePreferences(principal.userId, request)
                    call.respond(HttpStatusCode.OK, preferences)
                } catch (e: InvalidCategoryIdsException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid category IDs"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }
        }
    }
}
