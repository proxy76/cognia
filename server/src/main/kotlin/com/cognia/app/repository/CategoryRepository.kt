package com.cognia.app.repository

import com.cognia.app.database.CategoriesTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

data class CategoryRow(
    val id: String,
    val name: String,
    val slug: String
)

class CategoryRepository {

    fun findAll(): List<CategoryRow> = transaction {
        CategoriesTable.selectAll()
            .map { it.toCategoryRow() }
    }

    fun findById(id: String): CategoryRow? = transaction {
        CategoriesTable.selectAll().where { CategoriesTable.id eq id }
            .map { it.toCategoryRow() }
            .singleOrNull()
    }

    fun findBySlug(slug: String): CategoryRow? = transaction {
        CategoriesTable.selectAll().where { CategoriesTable.slug eq slug }
            .map { it.toCategoryRow() }
            .singleOrNull()
    }

    fun findByName(name: String): CategoryRow? = transaction {
        CategoriesTable.selectAll().where { CategoriesTable.name eq name }
            .map { it.toCategoryRow() }
            .singleOrNull()
    }

    fun findByIds(ids: List<String>): List<CategoryRow> = transaction {
        CategoriesTable.selectAll().where { CategoriesTable.id inList ids }
            .map { it.toCategoryRow() }
    }

    fun create(name: String, slug: String): CategoryRow = transaction {
        val id = UUID.randomUUID().toString()
        CategoriesTable.insert {
            it[CategoriesTable.id] = id
            it[CategoriesTable.name] = name
            it[CategoriesTable.slug] = slug
        }
        CategoryRow(id, name, slug)
    }

    fun update(id: String, name: String, slug: String): CategoryRow? = transaction {
        val updated = CategoriesTable.update({ CategoriesTable.id eq id }) {
            it[CategoriesTable.name] = name
            it[CategoriesTable.slug] = slug
        }
        if (updated > 0) CategoryRow(id, name, slug) else null
    }

    fun delete(id: String): Boolean = transaction {
        CategoriesTable.deleteWhere { CategoriesTable.id eq id } > 0
    }

    fun count(): Long = transaction {
        CategoriesTable.selectAll().count()
    }

    private fun ResultRow.toCategoryRow() = CategoryRow(
        id = this[CategoriesTable.id],
        name = this[CategoriesTable.name],
        slug = this[CategoriesTable.slug]
    )
}
