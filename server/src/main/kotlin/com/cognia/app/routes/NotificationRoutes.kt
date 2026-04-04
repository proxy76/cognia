package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.config.AppConfig
import com.cognia.app.dto.common.StatusResponse
import com.cognia.app.dto.notification.UnreadCountResponse
import com.cognia.app.service.NotificationService
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import org.koin.ktor.ext.inject

fun Route.notificationRoutes() {
    val notificationService by application.inject<NotificationService>()

    route("/api/v1/notifications") {
        authenticate("auth-jwt") {
            get {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val result = notificationService.getNotifications(principal.userId, page, limit)
                call.respond(HttpStatusCode.OK, result)
            }

            get("/unread-count") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val count = notificationService.getUnreadCount(principal.userId)
                call.respond(HttpStatusCode.OK, UnreadCountResponse(count))
            }

            post("/{id}/read") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val notificationId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing notification id"))
                val success = notificationService.markAsRead(notificationId, principal.userId)
                if (success) {
                    call.respond(HttpStatusCode.OK, StatusResponse("ok"))
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorBody("Notification not found"))
                }
            }

            post("/read-all") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val count = notificationService.markAllAsRead(principal.userId)
                call.respond(HttpStatusCode.OK, StatusResponse("marked $count as read"))
            }
        }
    }
}

fun Route.notificationWebSocket() {
    val notificationService by application.inject<NotificationService>()
    val config by application.inject<AppConfig>()

    webSocket("/ws/notifications") {
        val token = call.request.queryParameters["token"]
        if (token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing token"))
            return@webSocket
        }

        val userId = try {
            val verifier = JWT.require(Algorithm.HMAC256(config.jwt.secret))
                .withIssuer(config.jwt.issuer)
                .build()
            val decoded = verifier.verify(token)
            decoded.subject
        } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            return@webSocket
        }

        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid token"))
            return@webSocket
        }

        notificationService.addConnection(userId, this)
        try {
            for (frame in incoming) {
                // Client can send ping/pong or other messages; we just keep the connection alive
            }
        } finally {
            notificationService.removeConnection(userId, this)
        }
    }
}
