package com.cognia.app.auth

import io.ktor.server.auth.*

data class UserPrincipal(
    val userId: String,
    val email: String,
    val role: String
) : Principal
