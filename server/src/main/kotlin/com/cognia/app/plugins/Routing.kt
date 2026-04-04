package com.cognia.app.plugins

import com.cognia.app.routes.authRoutes
import com.cognia.app.routes.categoryRoutes
import com.cognia.app.routes.oauthRoutes
import com.cognia.app.routes.onboardingRoutes
import com.cognia.app.routes.profileRoutes
import com.cognia.app.routes.recommendationRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
        authRoutes()
        oauthRoutes()
        profileRoutes()
        categoryRoutes()
        onboardingRoutes()
        recommendationRoutes()
    }
}
