package com.cognia.app.repository

import com.cognia.app.database.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

data class FeedVideoRow(
    val id: String,
    val title: String,
    val creatorId: String,
    val creatorName: String,
    val thumbnailUrl: String?,
    val videoUrl: String?,
    val categoryName: String,
    val difficulty: String?,
    val hasQuiz: Boolean,
    val viewCount: Long
)

class FeedRepository {

    /**
     * Get published videos ordered by view count (popularity) for the For You feed.
     * Prioritises videos in the user's subscribed categories when [userCategoryIds] is provided.
     */
    fun getForYouFeed(
        userCategoryIds: List<String>,
        limit: Int,
        offset: Long
    ): List<FeedVideoRow> = transaction {
        val viewCountExpr = ContentViewsTable.id.count()

        // Left-join videos -> users, categories, content_views, quizzes
        val baseQuery = VideosTable
            .join(UsersTable, JoinType.INNER, VideosTable.creatorId, UsersTable.id)
            .join(CategoriesTable, JoinType.INNER, VideosTable.categoryId, CategoriesTable.id)
            .join(ContentViewsTable, JoinType.LEFT) {
                (ContentViewsTable.contentId eq VideosTable.id) and
                    (ContentViewsTable.contentType eq stringLiteral("VIDEO"))
            }
            .join(QuizzesTable, JoinType.LEFT, VideosTable.id, QuizzesTable.videoId)
            .select(
                VideosTable.id,
                VideosTable.title,
                VideosTable.creatorId,
                UsersTable.displayName,
                VideosTable.thumbnailUrl,
                VideosTable.videoUrl,
                CategoriesTable.name,
                VideosTable.difficulty,
                QuizzesTable.id,
                viewCountExpr
            )
            .where { VideosTable.status eq "PUBLISHED" }
            .groupBy(VideosTable.id)

        // Order: user categories first (if available), then by popularity
        if (userCategoryIds.isNotEmpty()) {
            val priorityExpr = Expression.build {
                case()
                    .When(VideosTable.categoryId inList userCategoryIds, intLiteral(0))
                    .Else(intLiteral(1))
            }
            baseQuery
                .orderBy(priorityExpr to SortOrder.ASC, viewCountExpr to SortOrder.DESC)
        } else {
            baseQuery
                .orderBy(viewCountExpr to SortOrder.DESC)
        }

        baseQuery
            .limit(limit)
            .offset(offset)
            .map { row ->
                FeedVideoRow(
                    id = row[VideosTable.id],
                    title = row[VideosTable.title],
                    creatorId = row[VideosTable.creatorId],
                    creatorName = row[UsersTable.displayName],
                    thumbnailUrl = row[VideosTable.thumbnailUrl],
                    videoUrl = row[VideosTable.videoUrl],
                    categoryName = row[CategoriesTable.name],
                    difficulty = row[VideosTable.difficulty],
                    hasQuiz = row[QuizzesTable.id] != null,
                    viewCount = row[viewCountExpr]
                )
            }
    }

    /**
     * Deep Dive feed: published videos filtered to the user's selected categories,
     * ordered by recency.
     */
    fun getDeepDiveFeed(
        userCategoryIds: List<String>,
        limit: Int,
        offset: Long
    ): List<FeedVideoRow> = transaction {
        val viewCountExpr = ContentViewsTable.id.count()

        val query = VideosTable
            .join(UsersTable, JoinType.INNER, VideosTable.creatorId, UsersTable.id)
            .join(CategoriesTable, JoinType.INNER, VideosTable.categoryId, CategoriesTable.id)
            .join(ContentViewsTable, JoinType.LEFT) {
                (ContentViewsTable.contentId eq VideosTable.id) and
                    (ContentViewsTable.contentType eq stringLiteral("VIDEO"))
            }
            .join(QuizzesTable, JoinType.LEFT, VideosTable.id, QuizzesTable.videoId)
            .select(
                VideosTable.id,
                VideosTable.title,
                VideosTable.creatorId,
                UsersTable.displayName,
                VideosTable.thumbnailUrl,
                VideosTable.videoUrl,
                CategoriesTable.name,
                VideosTable.difficulty,
                QuizzesTable.id,
                viewCountExpr
            )
            .where {
                val published = VideosTable.status eq "PUBLISHED"
                if (userCategoryIds.isNotEmpty()) {
                    published and (VideosTable.categoryId inList userCategoryIds)
                } else {
                    published
                }
            }
            .groupBy(VideosTable.id)
            .orderBy(VideosTable.publishedAt to SortOrder.DESC)
            .limit(limit)
            .offset(offset)

        query.map { row ->
            FeedVideoRow(
                id = row[VideosTable.id],
                title = row[VideosTable.title],
                creatorId = row[VideosTable.creatorId],
                creatorName = row[UsersTable.displayName],
                thumbnailUrl = row[VideosTable.thumbnailUrl],
                videoUrl = row[VideosTable.videoUrl],
                categoryName = row[CategoriesTable.name],
                difficulty = row[VideosTable.difficulty],
                hasQuiz = row[QuizzesTable.id] != null,
                viewCount = row[viewCountExpr]
            )
        }
    }
}
