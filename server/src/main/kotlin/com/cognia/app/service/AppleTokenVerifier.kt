package com.cognia.app.service

import java.util.Base64
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class AppleTokenPayload(val email: String, val name: String?)

interface AppleTokenVerifier {
    fun verify(identityToken: String, authorizationCode: String): AppleTokenPayload?
}

/**
 * Development implementation that decodes the JWT payload without cryptographic verification.
 * TODO: For production, verify the token signature against Apple's JWKS
 *       (https://appleid.apple.com/auth/keys) and validate issuer, audience, and expiry.
 *       Also exchange the authorizationCode for tokens via Apple's token endpoint.
 */
class DevAppleTokenVerifier : AppleTokenVerifier {

    @Serializable
    private data class JwtPayload(
        val email: String? = null,
        val name: String? = null
    )

    private val json = Json { ignoreUnknownKeys = true }

    override fun verify(identityToken: String, authorizationCode: String): AppleTokenPayload? {
        return try {
            val parts = identityToken.split(".")
            if (parts.size != 3) return null

            val payloadJson = String(Base64.getUrlDecoder().decode(parts[1]))
            val payload = json.decodeFromString<JwtPayload>(payloadJson)

            val email = payload.email ?: return null

            AppleTokenPayload(
                email = email,
                name = payload.name
            )
        } catch (_: Exception) {
            null
        }
    }
}
