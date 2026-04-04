package com.cognia.app.model

object Leveling {
    const val POINTS_PER_LEVEL = 100

    fun levelForPoints(points: Long): Int = (points / POINTS_PER_LEVEL).toInt() + 1

    fun pointsForLevel(level: Int): Long = (level - 1).toLong() * POINTS_PER_LEVEL
}
