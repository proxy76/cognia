package com.cognia.app.routes

import kotlinx.serialization.Serializable

/**
 * Simple error body used by route handlers for inline error responses.
 * For auth-related errors, exceptions should be thrown and handled by StatusPages.
 */
@Serializable
data class ErrorBody(val error: String)
