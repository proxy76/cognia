package com.cognia.app.service

import java.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class GoogleTokenPayload(val email: String, val name: String?, val emailVerified: Boolean)

interface GoogleTokenVerifier {
    fun verify(idToken: String): GoogleTokenPayload?
}

/**
 * Development implementation that decodes the JWT payload without cryptographic verification.
 * TODO: For production, verify the token signature against Google's public keys
 *       (https://www.googleapis.com/oauth2/v3/certs) and validate issuer, audience, and expiry.
 */
class DevGoogleTokenVerifier : GoogleTokenVerifier {

    @Serializable
    private data class JwtPayload(
        val email: String? = null,
        val name: String? = null,
        val email_verified: Boolean? = null
    )

    private val json = Json { ignoreUnknownKeys = true }

    override fun verify(idToken: String): GoogleTokenPayload? {
        return try {
            val parts = idToken.split(".")
            if (parts.size != 3) return null

            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val payload = json.decodeFromString<JwtPayload>(payloadJson)

            val email = payload.email ?: return null

            GoogleTokenPayload(
                email = email,
                name = payload.name,
                emailVerified = payload.email_verified ?: false
            )
        } catch (_: Exception) {
            null
        }
    }
}
