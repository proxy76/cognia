package com.cognia.app.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.cognia.app.config.AppConfig
import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.repository.UserRepository
import org.mindrot.jbcrypt.BCrypt
import java.util.Date
import java.util.UUID

class AuthService(
    private val userRepository: UserRepository,
    private val config: AppConfig
) {

    fun register(request: RegisterRequest): AuthResponse {
        // Validate input
        require(request.email.isNotBlank()) { "Email must not be empty" }
        require(request.password.isNotBlank()) { "Password must not be empty" }
        require(request.displayName.isNotBlank()) { "Display name must not be empty" }
        require(request.email.contains("@") && request.email.contains(".")) { "Invalid email format" }
        require(request.password.length >= 8) { "Password must be at least 8 characters" }

        // Check if email already exists
        val existing = userRepository.findByEmail(request.email)
        if (existing != null) {
            throw EmailAlreadyExistsException(request.email)
        }

        // Hash password and create user
        val passwordHash = BCrypt.hashpw(request.password, BCrypt.gensalt())
        val user = userRepository.createUser(request.email, passwordHash, request.displayName)

        // Generate tokens
        return generateAuthResponse(user.id)
    }

    fun login(request: LoginRequest): AuthResponse {
        require(request.email.isNotBlank()) { "Email must not be empty" }
        require(request.password.isNotBlank()) { "Password must not be empty" }

        val user = userRepository.findByEmail(request.email)
            ?: throw InvalidCredentialsException()

        if (user.passwordHash == null || !BCrypt.checkpw(request.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        return generateAuthResponse(user.id)
    }

    private fun generateAuthResponse(userId: String): AuthResponse {
        val token = generateJwt(userId)
        val refreshToken = UUID.randomUUID().toString()

        // Store refresh token hash
        val refreshTokenHash = BCrypt.hashpw(refreshToken, BCrypt.gensalt())
        val expiresAt = Date(System.currentTimeMillis() + config.jwt.refreshExpirationMs).toString()
        userRepository.saveRefreshToken(userId, refreshTokenHash, expiresAt)

        return AuthResponse(
            userId = userId,
            token = token,
            refreshToken = refreshToken
        )
    }

    private fun generateJwt(userId: String): String {
        return JWT.create()
            .withIssuer(config.jwt.issuer)
            .withSubject(userId)
            .withExpiresAt(Date(System.currentTimeMillis() + config.jwt.expirationMs))
            .sign(Algorithm.HMAC256(config.jwt.secret))
    }
}

class EmailAlreadyExistsException(email: String) : RuntimeException("Email already in use: $email")
class InvalidCredentialsException : RuntimeException("Invalid email or password")
