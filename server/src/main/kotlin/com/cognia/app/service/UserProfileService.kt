package com.cognia.app.service

import com.cognia.app.dto.profile.PublicProfileResponse
import com.cognia.app.dto.profile.UpdateProfileRequest
import com.cognia.app.dto.profile.UserProfileResponse
import com.cognia.app.repository.UserProfileRepository

class UserProfileService(
    private val userProfileRepository: UserProfileRepository
) {

    fun getMyProfile(userId: String): UserProfileResponse {
        return userProfileRepository.getFullProfile(userId)
            ?: throw UserNotFoundException(userId)
    }

    fun updateMyProfile(userId: String, request: UpdateProfileRequest): UserProfileResponse {
        val updated = userProfileRepository.updateProfile(userId, request)
        if (!updated) throw UserNotFoundException(userId)
        return userProfileRepository.getFullProfile(userId)
            ?: throw UserNotFoundException(userId)
    }

    fun getPublicProfile(userId: String): PublicProfileResponse? {
        return userProfileRepository.getPublicProfile(userId)
    }
}

class UserNotFoundException(userId: String) : RuntimeException("User not found: $userId")
