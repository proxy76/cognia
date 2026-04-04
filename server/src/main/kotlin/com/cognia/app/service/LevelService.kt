package com.cognia.app.service

import com.cognia.app.database.UserProfilesTable
import com.cognia.app.model.Leveling
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class LevelUpResult(
    val oldLevel: Int,
    val newLevel: Int,
    val leveledUp: Boolean
)

class LevelService {

    fun calculateLevel(totalPoints: Int): Int {
        return Leveling.levelForPoints(totalPoints.toLong())
    }

    /**
     * Recalculates the user's level based on new total points.
     * Updates the DB if a level-up occurred.
     * Returns the result indicating whether a level-up happened.
     */
    fun updateUserLevel(userId: String, newTotalPoints: Int): LevelUpResult {
        val oldLevel = transaction {
            UserProfilesTable.selectAll()
                .where { UserProfilesTable.userId eq userId }
                .single()[UserProfilesTable.level]
        }

        val newLevel = calculateLevel(newTotalPoints)

        if (newLevel > oldLevel) {
            transaction {
                UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                    it[level] = newLevel
                }
            }
            return LevelUpResult(oldLevel, newLevel, leveledUp = true)
        }

        return LevelUpResult(oldLevel, newLevel, leveledUp = false)
    }
}
