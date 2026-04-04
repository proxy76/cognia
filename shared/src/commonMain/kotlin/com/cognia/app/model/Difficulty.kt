package com.cognia.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class Difficulty(val pointsPerCorrectAnswer: Int) {
    EASY(10),
    MEDIUM(20),
    HARD(30);

    companion object {
        const val BASE_POINTS = 10
    }
}
