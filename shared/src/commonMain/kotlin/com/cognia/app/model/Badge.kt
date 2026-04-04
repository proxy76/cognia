package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconUrl: String,
    val criteria: String
)
