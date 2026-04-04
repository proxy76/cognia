package com.cognia.app.service

import com.cognia.app.database.*
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.search.*
import com.cognia.app.dto.user.CategorySummary
import com.cognia.app.dto.user.UserSummary
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SearchService {

    fun search(query: String, type: String?, limit: Int, offset: Long): SearchResultsResponse {
        val likePattern = "%${query}%"
        val results = transaction {
            val videos = if (type == null || type == "videos") searchVideos(likePattern, limit, offset) else emptyList()
            val quizzes = if (type == null || type == "quizzes") searchQuizzes(likePattern, limit, offset) else emptyList()
            val creators = if (type == null || type == "creators") searchCreators(likePattern, limit, offset) else emptyList()
            val categories = if (type == null || type == "categories") searchCategories(likePattern, limit, offset) else emptyList()

            SearchResultGroups(
                videos = videos,
                quizzes = quizzes,
                creators = creators,
                categories = categories
            )
        }

        // Determine if there are more results in any category
        val hasMore = results.videos.size >= limit ||
            results.quizzes.size >= limit ||
            results.creators.size >= limit ||
            results.categories.size >= limit

        return SearchResultsResponse(
            query = query,
            results = results,
            page = (offset / limit + 1).toInt(),
            hasMore = hasMore
        )
    }

    fun getSearchHistory(userId: String): RecentSearchesResponse = transaction {
        val searches = SearchHistoryTable.selectAll()
            .where { SearchHistoryTable.userId eq userId }
            .orderBy(SearchHistoryTable.searchedAt to SortOrder.DESC)
            .limit(20)
            .map { it[SearchHistoryTable.query] }
            .distinct()

        RecentSearchesResponse(recentSearches = searches)
    }

    fun saveSearchQuery(userId: String, query: String) = transaction {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC).toString()
        SearchHistoryTable.insert {
            it[SearchHistoryTable.id] = UUID.randomUUID().toString()
            it[SearchHistoryTable.userId] = userId
            it[SearchHistoryTable.query] = query
            it[SearchHistoryTable.searchedAt] = now
        }
    }

    private fun searchVideos(likePattern: String, limit: Int, offset: Long): List<VideoSearchResult> {
        return VideosTable
            .join(UsersTable, JoinType.INNER, VideosTable.creatorId, UsersTable.id)
            .join(CategoriesTable, JoinType.INNER, VideosTable.categoryId, CategoriesTable.id)
            .selectAll()
            .where {
                (VideosTable.status eq "PUBLISHED") and
                    ((VideosTable.title.lowerCase() like likePattern.lowercase()) or
                        (VideosTable.description.lowerCase() like likePattern.lowercase()))
            }
            .limit(limit)
            .offset(offset)
            .map { row ->
                VideoSearchResult(
                    id = row[VideosTable.id],
                    title = row[VideosTable.title],
                    creator = UserSummary(
                        id = row[UsersTable.id],
                        displayName = row[UsersTable.displayName],
                        avatarUrl = row[UsersTable.avatarUrl]
                    ),
                    thumbnailUrl = row[VideosTable.thumbnailUrl],
                    category = CategorySummary(
                        id = row[CategoriesTable.id],
                        name = row[CategoriesTable.name]
                    )
                )
            }
    }

    private fun searchQuizzes(likePattern: String, limit: Int, offset: Long): List<QuizSearchResult> {
        return QuizzesTable
            .join(CategoriesTable, JoinType.INNER, QuizzesTable.categoryId, CategoriesTable.id)
            .selectAll()
            .where {
                (QuizzesTable.status eq "PUBLISHED") and
                    (QuizzesTable.title.lowerCase() like likePattern.lowercase())
            }
            .limit(limit)
            .offset(offset)
            .map { row ->
                QuizSearchResult(
                    id = row[QuizzesTable.id],
                    title = row[QuizzesTable.title],
                    quizType = row[QuizzesTable.quizType],
                    category = CategorySummary(
                        id = row[CategoriesTable.id],
                        name = row[CategoriesTable.name]
                    )
                )
            }
    }

    private fun searchCreators(likePattern: String, limit: Int, offset: Long): List<CreatorSearchResult> {
        val followerCountExpr = FollowsTable.followerId.count()

        return UsersTable
            .join(FollowsTable, JoinType.LEFT, UsersTable.id, FollowsTable.followedId)
            .select(
                UsersTable.id,
                UsersTable.displayName,
                UsersTable.avatarUrl,
                followerCountExpr
            )
            .where {
                (UsersTable.displayName.lowerCase() like likePattern.lowercase()) and
                    (UsersTable.role inList listOf("REGULAR_CREATOR", "LICENSED_CREATOR", "ADMIN"))
            }
            .groupBy(UsersTable.id, UsersTable.displayName, UsersTable.avatarUrl)
            .limit(limit)
            .offset(offset)
            .map { row ->
                CreatorSearchResult(
                    id = row[UsersTable.id],
                    displayName = row[UsersTable.displayName],
                    avatarUrl = row[UsersTable.avatarUrl],
                    followerCount = row[followerCountExpr].toInt()
                )
            }
    }

    private fun searchCategories(likePattern: String, limit: Int, offset: Long): List<CategoryResponse> {
        return CategoriesTable.selectAll()
            .where { CategoriesTable.name.lowerCase() like likePattern.lowercase() }
            .limit(limit)
            .offset(offset)
            .map { row ->
                CategoryResponse(
                    id = row[CategoriesTable.id],
                    name = row[CategoriesTable.name],
                    slug = row[CategoriesTable.slug]
                )
            }
    }
}
