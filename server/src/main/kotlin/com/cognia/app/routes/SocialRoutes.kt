package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.service.FollowService
import com.cognia.app.service.FriendRequestAlreadyPendingException
import com.cognia.app.service.FriendRequestNotFoundException
import com.cognia.app.service.FriendshipAlreadyExistsException
import com.cognia.app.service.FriendshipService
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.socialRoutes() {
    val followService by application.inject<FollowService>()
    val friendshipService by application.inject<FriendshipService>()

    route("/api/v1") {
        authenticate("auth-jwt") {

            // ── Follow System ──────────────────────────────────────────

            post("/users/{id}/follow") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val targetId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user id"))

                try {
                    val result = followService.follow(principal.userId, targetId)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            delete("/users/{id}/follow") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val targetId = call.parameters["id"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user id"))

                val result = followService.unfollow(principal.userId, targetId)
                call.respond(HttpStatusCode.OK, result)
            }

            get("/users/{id}/followers") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val targetId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user id"))

                val result = followService.getFollowers(targetId)
                call.respond(HttpStatusCode.OK, result)
            }

            get("/users/{id}/following") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val targetId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user id"))

                val result = followService.getFollowing(targetId)
                call.respond(HttpStatusCode.OK, result)
            }

            // ── Friend Request System ──────────────────────────────────

            post("/friends/request/{userId}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val targetId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing userId"))

                try {
                    val result = friendshipService.sendRequest(principal.userId, targetId)
                    call.respond(HttpStatusCode.Created, result)
                } catch (e: FriendshipAlreadyExistsException) {
                    call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Already friends"))
                } catch (e: FriendRequestAlreadyPendingException) {
                    call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Request already pending"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            post("/friends/accept/{requestId}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val requestId = call.parameters["requestId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing requestId"))

                try {
                    val result = friendshipService.acceptRequest(requestId, principal.userId)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: FriendRequestNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Not found"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            post("/friends/decline/{requestId}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))
                val requestId = call.parameters["requestId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing requestId"))

                try {
                    val result = friendshipService.declineRequest(requestId, principal.userId)
                    call.respond(HttpStatusCode.OK, result)
                } catch (e: FriendRequestNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Not found"))
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                }
            }

            get("/friends") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val result = friendshipService.getFriends(principal.userId)
                call.respond(HttpStatusCode.OK, result)
            }

            get("/friends/requests") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val result = friendshipService.getPendingRequests(principal.userId)
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
}
