package com.cognia.app.dto.search

import com.cognia.app.dto.onboarding.CategoryResponse
import com.cognia.app.dto.user.CategorySummary
import com.cognia.app.dto.user.UserSummary
import kotlinx.serialization.Serializable

@Serializable
data class SearchResultsResponse(
    val query: String,
    val results: SearchResultGroups,
    val nextCursor: String? = null
)

@Serializable
data class SearchResultGroups(
    val videos: List<VideoSearchResult> = emptyList(),
    val quizzes: List<QuizSearchResult> = emptyList(),
    val creators: List<CreatorSearchResult> = emptyList(),
    val categories: List<CategoryResponse> = emptyList()
)

@Serializable
data class VideoSearchResult(
    val id: String,
    val title: String,
    val creator: UserSummary,
    val thumbnailUrl: String? = null,
    val category: CategorySummary
)

@Serializable
data class QuizSearchResult(
    val id: String,
    val title: String,
    val quizType: String,
    val category: CategorySummary
)

@Serializable
data class CreatorSearchResult(
    val id: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val followerCount: Int = 0
)

@Serializable
data class RecentSearchesResponse(
    val recentSearches: List<String>
)
