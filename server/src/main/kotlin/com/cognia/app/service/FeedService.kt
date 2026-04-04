package com.cognia.app.service

import com.cognia.app.dto.feed.FeedItem
import com.cognia.app.dto.feed.FeedResponse
import com.cognia.app.repository.FeedRepository
import com.cognia.app.repository.PreferenceRepository

class FeedService(
    private val feedRepository: FeedRepository,
    private val preferenceRepository: PreferenceRepository
) {

    fun getForYouFeed(userId: String, page: Int, limit: Int): FeedResponse {
        val userCategories = preferenceRepository.getCategories(userId)
        val categoryIds = userCategories.map { it.id }
        val offset = ((page - 1) * limit).toLong()

        val videos = feedRepository.getForYouFeed(categoryIds, limit + 1, offset)
        val hasMore = videos.size > limit
        val items = videos.take(limit).map { it.toFeedItem() }

        return FeedResponse(items = items, page = page, hasMore = hasMore)
    }

    fun getDeepDiveFeed(userId: String, page: Int, limit: Int): FeedResponse {
        val userCategories = preferenceRepository.getCategories(userId)
        val categoryIds = userCategories.map { it.id }
        val offset = ((page - 1) * limit).toLong()

        val videos = feedRepository.getDeepDiveFeed(categoryIds, limit + 1, offset)
        val hasMore = videos.size > limit
        val items = videos.take(limit).map { it.toFeedItem() }

        return FeedResponse(items = items, page = page, hasMore = hasMore)
    }

    private fun com.cognia.app.repository.FeedVideoRow.toFeedItem() = FeedItem(
        id = id,
        title = title,
        creatorName = creatorName,
        creatorId = creatorId,
        thumbnailUrl = thumbnailUrl,
        categoryName = categoryName,
        difficulty = difficulty,
        hasQuiz = hasQuiz
    )
}
