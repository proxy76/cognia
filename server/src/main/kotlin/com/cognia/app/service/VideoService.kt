package com.cognia.app.service

import com.cognia.app.dto.content.VideoDetailResponse
import com.cognia.app.dto.content.VideoUpdateRequest
import com.cognia.app.repository.ModerationRepository
import com.cognia.app.repository.VideoRepository

class VideoService(
    private val videoRepository: VideoRepository,
    private val aiModerationService: AiModerationService? = null,
    private val moderationRepository: ModerationRepository? = null
) {

    fun createDraft(
        creatorId: String,
        title: String,
        description: String?,
        categoryId: String,
        rawFilePath: String?
    ): VideoDetailResponse {
        val row = videoRepository.create(creatorId, title, description, categoryId, rawFilePath)
        return row.toResponse()
    }

    fun getVideo(id: String): VideoDetailResponse? {
        return videoRepository.findById(id)?.toResponse()
    }

    fun getCreatorVideos(creatorId: String, limit: Int = 20, offset: Long = 0): List<VideoDetailResponse> {
        return videoRepository.findByCreatorId(creatorId, limit, offset).map { it.toResponse() }
    }

    fun updateMetadata(id: String, userId: String, request: VideoUpdateRequest): VideoDetailResponse {
        val video = videoRepository.findById(id)
            ?: throw VideoNotFoundException(id)

        if (video.creatorId != userId) {
            throw VideoAccessDeniedException("You do not own this video")
        }

        val updated = videoRepository.updateMetadata(
            id = id,
            title = request.title,
            description = request.description,
            categoryId = request.categoryId,
            difficulty = request.difficulty
        ) ?: throw VideoNotFoundException(id)

        return updated.toResponse()
    }

    fun submitForReview(id: String, userId: String): VideoDetailResponse {
        val video = videoRepository.findById(id)
            ?: throw VideoNotFoundException(id)

        if (video.creatorId != userId) {
            throw VideoAccessDeniedException("You do not own this video")
        }

        ContentStateMachine.validateTransition(video.status, "PENDING_REVIEW")

        // AI pre-screening: auto-reject high-confidence violations
        if (aiModerationService != null && moderationRepository != null) {
            val (aiResult, action) = aiModerationService.assessAndDecide(
                title = video.title,
                description = video.description,
                contentType = "VIDEO"
            )
            if (action == "reject") {
                val updated = videoRepository.updateStatus(id, "REJECTED")
                    ?: throw VideoNotFoundException(id)
                return updated.toResponse()
            }
        }

        val updated = videoRepository.updateStatus(id, "PENDING_REVIEW")
            ?: throw VideoNotFoundException(id)

        // Create moderation review entry
        moderationRepository?.createReview(id, "VIDEO", isPostPublication = false)

        return updated.toResponse()
    }

    fun publishDirect(id: String, userId: String, userRole: String): VideoDetailResponse {
        if (userRole != "LICENSED_CREATOR" && userRole != "ADMIN") {
            throw VideoAccessDeniedException("Only licensed creators can publish directly")
        }

        val video = videoRepository.findById(id)
            ?: throw VideoNotFoundException(id)

        if (video.creatorId != userId) {
            throw VideoAccessDeniedException("You do not own this video")
        }

        ContentStateMachine.validateTransition(video.status, "PUBLISHED")

        val updated = videoRepository.updateStatus(id, "PUBLISHED")
            ?: throw VideoNotFoundException(id)

        return updated.toResponse()
    }

    private fun VideoRepository.VideoRow.toResponse() = VideoDetailResponse(
        id = id,
        creatorId = creatorId,
        title = title,
        description = description,
        categoryId = categoryId,
        videoUrl = videoUrl,
        thumbnailUrl = thumbnailUrl,
        status = status,
        difficulty = difficulty,
        createdAt = createdAt,
        updatedAt = updatedAt,
        publishedAt = publishedAt
    )
}

class VideoNotFoundException(id: String) : RuntimeException("Video not found: $id")
class VideoAccessDeniedException(message: String) : RuntimeException(message)
