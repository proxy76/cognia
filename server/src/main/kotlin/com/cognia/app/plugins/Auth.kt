package com.cognia.app.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cognia.app.auth.UserPrincipal
import com.cognia.app.config.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import org.koin.ktor.ext.inject

fun Application.configureAuth() {
    val config by inject<AppConfig>()

    val jwtVerifier = JWT.require(Algorithm.HMAC256(config.jwt.secret))
        .withIssuer(config.jwt.issuer)
        .build()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = "cognia"
            verifier(jwtVerifier)
            validate { credential ->
                val userId = credential.payload.subject
                val email = credential.payload.getClaim("email")?.asString()
                val role = credential.payload.getClaim("role")?.asString()

                if (userId != null && email != null && role != null) {
                    UserPrincipal(userId, email, role)
                } else {
                    null
                }
            }
            challenge { _, _ ->
                call.respond(io.ktor.http.HttpStatusCode.Unauthorized)
            }
        }
    }
}
