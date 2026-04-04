package com.cognia.app.plugins

import com.cognia.app.routes.authRoutes
import com.cognia.app.routes.categoryRoutes
import com.cognia.app.routes.feedRoutes
import com.cognia.app.routes.oauthRoutes
import com.cognia.app.routes.onboardingRoutes
import com.cognia.app.routes.profileRoutes
import com.cognia.app.routes.recommendationRoutes
import com.cognia.app.routes.searchRoutes
import com.cognia.app.routes.socialRoutes
import com.cognia.app.routes.chatRoutes
import com.cognia.app.routes.trackingRoutes
import com.cognia.app.routes.videoRoutes
import com.cognia.app.routes.videoStreamRoutes
import com.cognia.app.routes.gamificationRoutes
import com.cognia.app.routes.notificationRoutes
import com.cognia.app.routes.notificationWebSocket
import com.cognia.app.routes.analyticsRoutes
import com.cognia.app.routes.licenseRoutes
import com.cognia.app.routes.moderationRoutes
import com.cognia.app.routes.aiRoutes
import com.cognia.app.routes.blockRoutes
import com.cognia.app.routes.adminRoutes
import com.cognia.app.routes.quizRoutes
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
        videoRoutes()
        videoStreamRoutes()
        gamificationRoutes()
        quizRoutes()
        feedRoutes()
        trackingRoutes()
        searchRoutes()
        socialRoutes()
        chatRoutes()
        notificationRoutes()
        notificationWebSocket()
        analyticsRoutes()
        moderationRoutes()
        licenseRoutes()
        aiRoutes()
        blockRoutes()
        adminRoutes()
    }
}
