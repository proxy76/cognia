package com.cognia.app.repository

import com.cognia.app.database.CategoriesTable
import com.cognia.app.database.UserCategoriesTable
import com.cognia.app.database.UserProfilesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class PreferenceRepository {

    fun getCategories(userId: String): List<CategoryRow> = transaction {
        UserCategoriesTable
            .join(CategoriesTable, JoinType.INNER, UserCategoriesTable.categoryId, CategoriesTable.id)
            .selectAll()
            .where { UserCategoriesTable.userId eq userId }
            .map {
                CategoryRow(
                    id = it[CategoriesTable.id],
                    name = it[CategoriesTable.name],
                    slug = it[CategoriesTable.slug]
                )
            }
    }

    fun getSelfDescription(userId: String): String? = transaction {
        UserProfilesTable.selectAll()
            .where { UserProfilesTable.userId eq userId }
            .singleOrNull()
            ?.get(UserProfilesTable.selfDescription)
    }

    fun savePreferences(userId: String, categoryIds: List<String>, selfDescription: String?) = transaction {
        // Replace all category links for user
        UserCategoriesTable.deleteWhere { UserCategoriesTable.userId eq userId }

        for (categoryId in categoryIds) {
            UserCategoriesTable.insert {
                it[UserCategoriesTable.userId] = userId
                it[UserCategoriesTable.categoryId] = categoryId
            }
        }

        // Update selfDescription in UserProfilesTable
        if (selfDescription != null) {
            UserProfilesTable.update({ UserProfilesTable.userId eq userId }) {
                it[UserProfilesTable.selfDescription] = selfDescription
            }
        }
    }
}
