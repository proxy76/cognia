package com.cognia.app.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Wraps routes with role-based authorization. Only users whose role is
 * in the given [roles] list (or ADMIN, which always passes) are allowed.
 * Returns 403 Forbidden when the principal exists but lacks the required role,
 * and 401 Unauthorized when no principal is present.
 */
fun Route.authorize(vararg roles: String, build: Route.() -> Unit) {
    val requiredRoles = roles.toSet()

    // Create a unique plugin name based on the roles to avoid conflicts
    val pluginName = "RoleAuth_${requiredRoles.sorted().joinToString("_")}"

    val authorizationPlugin = createRouteScopedPlugin(pluginName) {
        on(AuthenticationChecked) { call ->
            val principal = call.principal<UserPrincipal>()
            if (principal == null) {
                call.respond(HttpStatusCode.Unauthorized)
                return@on
            }
            if (principal.role != "ADMIN" && principal.role !in requiredRoles) {
                call.respond(HttpStatusCode.Forbidden)
                return@on
            }
        }
    }

    // Create a child route so the plugin only applies within this scope
    val authorizedRoute = createChild(object : RouteSelector() {
        override suspend fun evaluate(context: RoutingResolveContext, segmentIndex: Int) =
            RouteSelectorEvaluation.Transparent

        override fun toString(): String = "(authorize: ${requiredRoles.joinToString()})"
    })

    authorizedRoute.install(authorizationPlugin)
    authorizedRoute.build()
}
