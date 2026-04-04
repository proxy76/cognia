package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.dto.moderation.BlockedUserListResponse
import com.cognia.app.service.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.blockRoutes() {
    val blockService by application.inject<BlockService>()

    route("/api/v1/users") {
        authenticate("auth-jwt") {

            // POST /{userId}/block — block a user
            post("/{userId}/block") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val targetUserId = call.parameters["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user ID"))

                try {
                    val result = blockService.blockUser(principal.userId, targetUserId)
                    call.respond(HttpStatusCode.Created, result)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                } catch (e: BlockedUserNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "User not found"))
                } catch (e: AlreadyBlockedException) {
                    call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Already blocked"))
                }
            }

            // DELETE /{userId}/block — unblock a user
            delete("/{userId}/block") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@delete call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val targetUserId = call.parameters["userId"]
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing user ID"))

                try {
                    blockService.unblockUser(principal.userId, targetUserId)
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: BlockNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Block not found"))
                }
            }

            // GET /me/blocked — list blocked users
            get("/me/blocked") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val blockedUsers = blockService.getBlockedUsers(principal.userId)
                call.respond(HttpStatusCode.OK, BlockedUserListResponse(users = blockedUsers))
            }
        }
    }
}
