package com.cognia.app.service

import com.cognia.app.dto.social.FollowStatusResponse
import com.cognia.app.dto.social.FollowerListResponse
import com.cognia.app.dto.social.FollowingListResponse
import com.cognia.app.dto.user.UserSummary
import com.cognia.app.repository.FollowRepository

class FollowService(private val followRepository: FollowRepository) {

    fun follow(followerId: String, followedId: String): FollowStatusResponse {
        require(followerId != followedId) { "Cannot follow yourself" }
        if (followRepository.isFollowing(followerId, followedId)) {
            return FollowStatusResponse(following = true)
        }
        followRepository.follow(followerId, followedId)
        return FollowStatusResponse(following = true)
    }

    fun unfollow(followerId: String, followedId: String): FollowStatusResponse {
        followRepository.unfollow(followerId, followedId)
        return FollowStatusResponse(following = false)
    }

    fun isFollowing(followerId: String, followedId: String): Boolean {
        return followRepository.isFollowing(followerId, followedId)
    }

    fun getFollowers(userId: String): FollowerListResponse {
        val followers = followRepository.getFollowers(userId).map {
            UserSummary(id = it.id, displayName = it.displayName, avatarUrl = it.avatarUrl)
        }
        return FollowerListResponse(followers = followers)
    }

    fun getFollowing(userId: String): FollowingListResponse {
        val following = followRepository.getFollowing(userId).map {
            UserSummary(id = it.id, displayName = it.displayName, avatarUrl = it.avatarUrl)
        }
        return FollowingListResponse(following = following)
    }
}
