package com.cognia.app.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cognia.app.config.AppConfig
import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.chat.*
import com.cognia.app.service.ChatService
import com.cognia.app.service.ConversationAccessDeniedException
import com.cognia.app.service.NotFriendsException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import java.util.concurrent.ConcurrentHashMap

private val chatSessions = ConcurrentHashMap<String, MutableList<WebSocketServerSession>>()
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun Route.chatRoutes() {
    val chatService by application.inject<ChatService>()
    val appConfig by application.inject<AppConfig>()

    route("/api/v1/chat") {
        authenticate("auth-jwt") {

            get("/conversations") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val result = chatService.getConversations(principal.userId)
                call.respond(HttpStatusCode.OK, result)
            }

            post("/conversations") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                try {
                    val request = call.receive<CreateConversationRequest>()
                    val result = chatService.createConversation(principal.userId, request.participantId)
                    call.respond(HttpStatusCode.Created, result)
                } catch (e: NotFriendsException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Not friends"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            get("/conversations/{id}/messages") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val conversationId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing conversation id"))
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
                val cursor = call.request.queryParameters["cursor"]

                try {
                    val result = chatService.getMessages(conversationId, principal.userId, limit, cursor)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: ConversationAccessDeniedException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                }
            }

            post("/conversations/{id}/messages") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val conversationId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing conversation id"))

                try {
                    // Try parsing as text message first, then shared content
                    val body = call.receiveText()
                    val textRequest = runCatching { json.decodeFromString<SendTextMessageRequest>(body) }.getOrNull()
                    val sharedRequest = if (textRequest == null) {
                        runCatching { json.decodeFromString<SendSharedPostRequest>(body) }.getOrNull()
                    } else null

                    val result = when {
                        textRequest != null -> chatService.sendMessage(
                            conversationId = conversationId,
                            senderId = principal.userId,
                            messageType = textRequest.messageType,
                            textContent = textRequest.textContent,
                            sharedContentId = null,
                            sharedContentType = null
                        )
                        sharedRequest != null -> chatService.sendMessage(
                            conversationId = conversationId,
                            senderId = principal.userId,
                            messageType = sharedRequest.messageType,
                            textContent = null,
                            sharedContentId = sharedRequest.sharedContentId,
                            sharedContentType = sharedRequest.sharedContentType
                        )
                        else -> return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Invalid message format"))
                    }

                    // Broadcast to WebSocket sessions
                    broadcastToConversation(chatService, conversationId, result)

                    call.respond(HttpStatusCode.Created, result)
                } catch (e: ConversationAccessDeniedException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                }
            }
        }
    }

    // ── WebSocket for real-time chat ────────────────────────────────

    webSocket("/ws/chat") {
        // Authenticate via token query param
        val token = call.request.queryParameters["token"]
        if (token.isNullOrBlank()) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing auth token"))
            return@webSocket
        }

        val userId = try {
            val verifier = JWT.require(Algorithm.HMAC256(appConfig.jwt.secret))
                .withIssuer(appConfig.jwt.issuer)
                .build()
            val decoded = verifier.verify(token)
            decoded.subject
        } catch (e: Exception) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid auth token"))
            return@webSocket
        }

        if (userId == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid auth token"))
            return@webSocket
        }

        // Register session
        val sessions = chatSessions.getOrPut(userId) { mutableListOf() }
        sessions.add(this)

        try {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try {
                        val wsMessage = json.decodeFromString<WebSocketChatMessage>(text)

                        val result = chatService.sendMessage(
                            conversationId = wsMessage.conversationId,
                            senderId = userId,
                            messageType = wsMessage.messageType,
                            textContent = wsMessage.textContent,
                            sharedContentId = wsMessage.sharedContentId,
                            sharedContentType = wsMessage.sharedContentType
                        )

                        // Broadcast to all participants
                        broadcastToConversation(chatService, wsMessage.conversationId, result)

                    } catch (e: Exception) {
                        val errorEvent = WebSocketChatEvent(type = "error", error = e.message)
                        send(Frame.Text(json.encodeToString(WebSocketChatEvent.serializer(), errorEvent)))
                    }
                }
            }
        } catch (_: ClosedReceiveChannelException) {
            // Client disconnected
        } finally {
            sessions.remove(this)
            if (sessions.isEmpty()) {
                chatSessions.remove(userId)
            }
        }
    }
}

private suspend fun broadcastToConversation(
    chatService: ChatService,
    conversationId: String,
    message: ChatMessageResponse
) {
    val participants = chatService.getConversationParticipants(conversationId) ?: return
    val event = WebSocketChatEvent(type = "message", message = message)
    val eventJson = json.encodeToString(WebSocketChatEvent.serializer(), event)

    listOf(participants.first, participants.second).forEach { participantId ->
        chatSessions[participantId]?.forEach { session ->
            try {
                session.send(Frame.Text(eventJson))
            } catch (_: Exception) {
                // Session may be closed
            }
        }
    }
}
