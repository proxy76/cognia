package com.cognia.app.routes

import com.cognia.app.dto.auth.AppleOAuthRequest
import com.cognia.app.dto.auth.GoogleOAuthRequest
import com.cognia.app.service.AppleTokenVerifier
import com.cognia.app.service.GoogleTokenVerifier
import com.cognia.app.service.OAuthService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.oauthRoutes() {
    val oauthService by application.inject<OAuthService>()
    val googleTokenVerifier by application.inject<GoogleTokenVerifier>()
    val appleTokenVerifier by application.inject<AppleTokenVerifier>()

    route("/api/v1/auth/oauth") {
        post("/google") {
            val request = call.receive<GoogleOAuthRequest>()
            val response = oauthService.authenticateWithGoogle(request.idToken, googleTokenVerifier)
            call.respond(HttpStatusCode.OK, response)
        }

        post("/apple") {
            val request = call.receive<AppleOAuthRequest>()
            val response = oauthService.authenticateWithApple(
                request.identityToken,
                request.authorizationCode,
                appleTokenVerifier
            )
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
