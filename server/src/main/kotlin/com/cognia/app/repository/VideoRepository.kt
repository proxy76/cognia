package com.cognia.app.repository

import com.cognia.app.database.VideosTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class VideoRepository {

    data class VideoRow(
        val id: String,
        val creatorId: String,
        val title: String,
        val description: String?,
        val categoryId: String,
        val videoUrl: String?,
        val thumbnailUrl: String?,
        val rawFilePath: String?,
        val status: String,
        val difficulty: String?,
        val createdAt: String,
        val updatedAt: String,
        val publishedAt: String?
    )

    fun create(creatorId: String, title: String, description: String?, categoryId: String, rawFilePath: String?): VideoRow = transaction {
        val id = UUID.randomUUID().toString()
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        VideosTable.insert {
            it[VideosTable.id] = id
            it[VideosTable.creatorId] = creatorId
            it[VideosTable.title] = title
            it[VideosTable.description] = description
            it[VideosTable.categoryId] = categoryId
            it[VideosTable.rawFilePath] = rawFilePath
            it[VideosTable.status] = "DRAFT"
            it[VideosTable.createdAt] = now
            it[VideosTable.updatedAt] = now
        }

        VideoRow(
            id = id,
            creatorId = creatorId,
            title = title,
            description = description,
            categoryId = categoryId,
            videoUrl = null,
            thumbnailUrl = null,
            rawFilePath = rawFilePath,
            status = "DRAFT",
            difficulty = null,
            createdAt = now,
            updatedAt = now,
            publishedAt = null
        )
    }

    fun findById(id: String): VideoRow? = transaction {
        VideosTable.selectAll().where { VideosTable.id eq id }
            .map { it.toVideoRow() }
            .singleOrNull()
    }

    fun findByCreatorId(creatorId: String, limit: Int = 20, offset: Long = 0): List<VideoRow> = transaction {
        VideosTable.selectAll()
            .where { VideosTable.creatorId eq creatorId }
            .orderBy(VideosTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(offset)
            .map { it.toVideoRow() }
    }

    fun updateMetadata(id: String, title: String?, description: String?, categoryId: String?, difficulty: String?): VideoRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        val updated = VideosTable.update({ VideosTable.id eq id }) {
            if (title != null) it[VideosTable.title] = title
            if (description != null) it[VideosTable.description] = description
            if (categoryId != null) it[VideosTable.categoryId] = categoryId
            if (difficulty != null) it[VideosTable.difficulty] = difficulty
            it[VideosTable.updatedAt] = now
        }

        if (updated > 0) findById(id) else null
    }

    fun updateStatus(id: String, status: String): VideoRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        val updated = VideosTable.update({ VideosTable.id eq id }) {
            it[VideosTable.status] = status
            it[VideosTable.updatedAt] = now
            if (status == "PUBLISHED") {
                it[VideosTable.publishedAt] = now
            }
        }

        if (updated > 0) findById(id) else null
    }

    fun updateProcessedUrls(id: String, videoUrl: String, thumbnailUrl: String): VideoRow? = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()

        val updated = VideosTable.update({ VideosTable.id eq id }) {
            it[VideosTable.videoUrl] = videoUrl
            it[VideosTable.thumbnailUrl] = thumbnailUrl
            it[VideosTable.updatedAt] = now
        }

        if (updated > 0) findById(id) else null
    }

    fun delete(id: String): Boolean = transaction {
        VideosTable.deleteWhere { VideosTable.id eq id } > 0
    }

    private fun ResultRow.toVideoRow() = VideoRow(
        id = this[VideosTable.id],
        creatorId = this[VideosTable.creatorId],
        title = this[VideosTable.title],
        description = this[VideosTable.description],
        categoryId = this[VideosTable.categoryId],
        videoUrl = this[VideosTable.videoUrl],
        thumbnailUrl = this[VideosTable.thumbnailUrl],
        rawFilePath = this[VideosTable.rawFilePath],
        status = this[VideosTable.status],
        difficulty = this[VideosTable.difficulty],
        createdAt = this[VideosTable.createdAt],
        updatedAt = this[VideosTable.updatedAt],
        publishedAt = this[VideosTable.publishedAt]
    )
}
