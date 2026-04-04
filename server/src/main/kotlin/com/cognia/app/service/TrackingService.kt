package com.cognia.app.service

import com.cognia.app.database.ContentSharesTable
import com.cognia.app.database.ContentViewsTable
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class TrackingService {

    fun trackView(userId: String, contentId: String, contentType: String) = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        ContentViewsTable.insert {
            it[ContentViewsTable.id] = UUID.randomUUID().toString()
            it[ContentViewsTable.userId] = userId
            it[ContentViewsTable.contentId] = contentId
            it[ContentViewsTable.contentType] = contentType
            it[ContentViewsTable.viewedAt] = now
        }
    }

    fun trackShare(userId: String, contentId: String, contentType: String) = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        ContentSharesTable.insert {
            it[ContentSharesTable.id] = UUID.randomUUID().toString()
            it[ContentSharesTable.userId] = userId
            it[ContentSharesTable.contentId] = contentId
            it[ContentSharesTable.contentType] = contentType
            it[ContentSharesTable.sharedAt] = now
        }
    }
}
