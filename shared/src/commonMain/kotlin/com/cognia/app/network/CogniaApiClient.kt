package com.cognia.app.network

import com.cognia.app.dto.auth.AuthResponse
import com.cognia.app.dto.auth.LoginRequest
import com.cognia.app.dto.auth.RegisterRequest
import com.cognia.app.dto.category.CategoryResponse
import com.cognia.app.dto.feed.FeedResponse
import com.cognia.app.dto.onboarding.PreferenceResponse
import com.cognia.app.dto.onboarding.RecommendRequest
import com.cognia.app.dto.onboarding.RecommendResponse
import com.cognia.app.dto.onboarding.SavePreferencesRequest
import com.cognia.app.dto.profile.UserProfileResponse
import com.cognia.app.dto.quiz.CreateQuizRequest
import com.cognia.app.dto.quiz.QuizDetailResponse
import com.cognia.app.dto.quiz.QuizAttemptRequest
import com.cognia.app.dto.quiz.AttemptResultResponse
import com.cognia.app.dto.search.RecentSearchesResponse
import com.cognia.app.dto.search.SearchResultsResponse
import com.cognia.app.dto.chat.ConversationListResponse
import com.cognia.app.dto.chat.MessageListResponse
import com.cognia.app.dto.chat.ChatMessageResponse
import com.cognia.app.dto.chat.SendTextMessageRequest
import com.cognia.app.dto.chat.SendSharedPostRequest
import com.cognia.app.dto.notification.NotificationListResponse
import com.cognia.app.dto.gamification.LeaderboardResponse
import com.cognia.app.dto.analytics.CreatorAnalyticsResponse
import com.cognia.app.dto.social.FollowStatusResponse
import com.cognia.app.dto.social.FollowerListResponse
import com.cognia.app.dto.social.FollowingListResponse
import com.cognia.app.dto.social.FriendListResponse
import com.cognia.app.dto.social.PendingRequestsResponse
import com.cognia.app.dto.moderation.*
import com.cognia.app.dto.gamification.BadgeListResponse
import com.cognia.app.dto.ai.*
import com.cognia.app.dto.content.VideoDetailResponse
import com.cognia.app.dto.content.VideoUploadResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Shared API client used by ViewModels to call the Cognia backend.
 * Wraps ktor HttpClient and provides typed suspend functions.
 */
class CogniaApiClient(
    private val client: HttpClient
) {
    // ── Auth ────────────────────────────────────────────────────────
    suspend fun register(email: String, password: String, displayName: String): ApiResult<AuthResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/auth/register")) {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(email, password, displayName))
            }
        }

    suspend fun login(email: String, password: String): ApiResult<AuthResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/auth/login")) {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
        }

    // ── Profile ─────────────────────────────────────────────────────
    suspend fun getMyProfile(): ApiResult<UserProfileResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/users/me")) }

    // ── Categories ──────────────────────────────────────────────────
    suspend fun getCategories(): ApiResult<List<CategoryResponse>> =
        safeCall { client.get(ApiConfig.apiUrl("/categories")) }

    // ── Feed ────────────────────────────────────────────────────────
    suspend fun getForYouFeed(page: Int = 1, limit: Int = 20): ApiResult<FeedResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/feed/foryou")) {
                parameter("page", page)
                parameter("limit", limit)
            }
        }

    suspend fun getDeepDiveFeed(page: Int = 1, limit: Int = 20): ApiResult<FeedResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/feed/deepdive")) {
                parameter("page", page)
                parameter("limit", limit)
            }
        }

    // ── Search ──────────────────────────────────────────────────────
    suspend fun search(query: String, type: String? = null, page: Int = 1): ApiResult<SearchResultsResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/search")) {
                parameter("q", query)
                if (type != null) parameter("type", type)
                parameter("page", page)
            }
        }

    suspend fun getSearchHistory(): ApiResult<RecentSearchesResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/search/history")) }

    // ── Onboarding ──────────────────────────────────────────────────
    suspend fun getRecommendations(selfDescription: String): ApiResult<RecommendResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/onboarding/recommend")) {
                contentType(ContentType.Application.Json)
                setBody(RecommendRequest(selfDescription = selfDescription))
            }
        }

    suspend fun savePreferences(categoryIds: List<String>, selfDescription: String?): ApiResult<PreferenceResponse> =
        safeCall {
            client.put(ApiConfig.apiUrl("/onboarding/preferences")) {
                contentType(ContentType.Application.Json)
                setBody(SavePreferencesRequest(categoryIds = categoryIds, selfDescription = selfDescription))
            }
        }

    // ── Quiz ────────────────────────────────────────────────────────
    suspend fun getQuiz(quizId: String): ApiResult<QuizDetailResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/quizzes/$quizId")) }

    suspend fun createQuiz(request: CreateQuizRequest): ApiResult<QuizDetailResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/quizzes")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    suspend fun submitQuizAttempt(quizId: String, answers: List<Int>): ApiResult<AttemptResultResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/quizzes/$quizId/attempt")) {
                contentType(ContentType.Application.Json)
                setBody(QuizAttemptRequest(answers = answers))
            }
        }

    // ── Chat ────────────────────────────────────────────────────────
    suspend fun getConversations(): ApiResult<ConversationListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/chat/conversations")) }

    suspend fun getMessages(conversationId: String, cursor: String? = null): ApiResult<MessageListResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/chat/conversations/$conversationId/messages")) {
                if (cursor != null) parameter("cursor", cursor)
            }
        }

    suspend fun sendTextMessage(conversationId: String, text: String): ApiResult<ChatMessageResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/chat/conversations/$conversationId/messages")) {
                contentType(ContentType.Application.Json)
                setBody(SendTextMessageRequest(messageType = "TEXT", textContent = text))
            }
        }

    suspend fun sendSharedPost(conversationId: String, contentId: String, contentType: String): ApiResult<ChatMessageResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/chat/conversations/$conversationId/messages")) {
                this.contentType(ContentType.Application.Json)
                setBody(SendSharedPostRequest(messageType = "SHARED_POST", sharedContentId = contentId, sharedContentType = contentType))
            }
        }

    // ── Notifications ───────────────────────────────────────────────
    suspend fun getNotifications(page: Int = 1, limit: Int = 20): ApiResult<NotificationListResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/notifications")) {
                parameter("page", page)
                parameter("limit", limit)
            }
        }

    suspend fun markNotificationRead(notificationId: String): ApiResult<Unit> =
        safeCall { client.post(ApiConfig.apiUrl("/notifications/$notificationId/read")) }

    suspend fun markAllNotificationsRead(): ApiResult<Unit> =
        safeCall { client.post(ApiConfig.apiUrl("/notifications/read-all")) }

    // ── Leaderboard ─────────────────────────────────────────────────
    suspend fun getLeaderboard(): ApiResult<LeaderboardResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/gamification/leaderboard")) }

    // ── Analytics ───────────────────────────────────────────────────
    suspend fun getCreatorAnalytics(): ApiResult<CreatorAnalyticsResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/analytics/creator")) }

    // ── Social ──────────────────────────────────────────────────────
    suspend fun follow(userId: String): ApiResult<FollowStatusResponse> =
        safeCall { client.post(ApiConfig.apiUrl("/users/$userId/follow")) }

    suspend fun unfollow(userId: String): ApiResult<FollowStatusResponse> =
        safeCall { client.delete(ApiConfig.apiUrl("/users/$userId/follow")) }

    suspend fun getFollowers(userId: String): ApiResult<FollowerListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/users/$userId/followers")) }

    suspend fun getFollowing(userId: String): ApiResult<FollowingListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/users/$userId/following")) }

    suspend fun sendFriendRequest(userId: String): ApiResult<Unit> =
        safeCall { client.post(ApiConfig.apiUrl("/friends/request/$userId")) }

    suspend fun acceptFriendRequest(requestId: String): ApiResult<Unit> =
        safeCall { client.post(ApiConfig.apiUrl("/friends/accept/$requestId")) }

    suspend fun declineFriendRequest(requestId: String): ApiResult<Unit> =
        safeCall { client.post(ApiConfig.apiUrl("/friends/decline/$requestId")) }

    suspend fun getFriends(): ApiResult<FriendListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/friends")) }

    suspend fun getPendingFriendRequests(): ApiResult<PendingRequestsResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/friends/requests")) }

    // ── Moderation (MODERATOR/ADMIN) ──────────────────────────────
    suspend fun getModerationQueue(): ApiResult<ModerationQueueResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/moderation/queue")) }

    suspend fun decideModerationReview(
        reviewId: String,
        decision: String,
        reason: String? = null,
        issueStrike: Boolean = false
    ): ApiResult<ModerationReviewResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/moderation/$reviewId/decide")) {
                contentType(ContentType.Application.Json)
                setBody(ModerationDecisionRequest(decision = decision, reason = reason, issueStrike = issueStrike))
            }
        }

    suspend fun getModerationReviews(): ApiResult<ModerationReviewListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/moderation/reviews")) }

    suspend fun getReports(): ApiResult<ReportListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/reports")) }

    suspend fun createReport(contentId: String, contentType: String, reason: String): ApiResult<ReportResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/reports")) {
                this.contentType(ContentType.Application.Json)
                setBody(ReportCreateRequest(contentId = contentId, contentType = contentType, reason = reason))
            }
        }

    suspend fun resolveReport(reportId: String, status: String, resolution: String? = null): ApiResult<ReportResponse> =
        safeCall {
            client.patch(ApiConfig.apiUrl("/reports/$reportId/resolve")) {
                contentType(ContentType.Application.Json)
                setBody(ReportResolveRequest(status = status, resolution = resolution))
            }
        }

    suspend fun getReportStats(): ApiResult<ReportStatsResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/reports/stats")) }

    // ── License Requests ────────────────────────────────────────────
    suspend fun getLicenseRequests(): ApiResult<LicenseRequestListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/license/requests")) }

    suspend fun approveLicenseRequest(requestId: String): ApiResult<LicenseRequestResponse> =
        safeCall { client.post(ApiConfig.apiUrl("/license/$requestId/approve")) }

    suspend fun rejectLicenseRequest(requestId: String, reason: String? = null): ApiResult<LicenseRequestResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/license/$requestId/reject")) {
                contentType(ContentType.Application.Json)
                setBody(LicenseRejectRequest(reason = reason))
            }
        }

    // ── Blocking ────────────────────────────────────────────────────
    suspend fun blockUser(userId: String): ApiResult<BlockedUserResponse> =
        safeCall { client.post(ApiConfig.apiUrl("/users/$userId/block")) }

    suspend fun unblockUser(userId: String): ApiResult<Unit> =
        safeCall { client.delete(ApiConfig.apiUrl("/users/$userId/block")) }

    suspend fun getBlockedUsers(): ApiResult<BlockedUserListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/users/me/blocked")) }

    // ── Badges ──────────────────────────────────────────────────────
    suspend fun getAllBadges(): ApiResult<BadgeListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/badges")) }

    suspend fun getMyBadges(): ApiResult<BadgeListResponse> =
        safeCall { client.get(ApiConfig.apiUrl("/users/me/badges")) }

    // ── AI ──────────────────────────────────────────────────────────
    suspend fun aiCategorize(title: String, description: String? = null): ApiResult<CategorizationResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/ai/categorize")) {
                contentType(ContentType.Application.Json)
                setBody(CategorizationRequest(title = title, description = description))
            }
        }

    suspend fun aiGenerateQuiz(request: QuizGenerateRequest): ApiResult<QuizGenerateResponse> =
        safeCall {
            client.post(ApiConfig.apiUrl("/ai/quiz-generate")) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }

    // ── Admin ───────────────────────────────────────────────────────
    suspend fun suspendUser(userId: String, durationDays: Int, reason: String): ApiResult<Unit> =
        safeCall {
            client.post(ApiConfig.apiUrl("/users/$userId/suspend")) {
                contentType(ContentType.Application.Json)
                setBody(SuspendUserRequest(durationDays = durationDays, reason = reason))
            }
        }

    suspend fun getAuditLog(limit: Int = 100, offset: Int = 0): ApiResult<AuditLogResponse> =
        safeCall {
            client.get(ApiConfig.apiUrl("/moderation/audit")) {
                parameter("limit", limit)
                parameter("offset", offset)
            }
        }

    // ── Video Upload ─────────────────────────────────────────────────
    suspend fun uploadVideo(
        title: String,
        description: String?,
        categoryId: String,
        fileBytes: ByteArray,
        fileName: String
    ): ApiResult<VideoUploadResponse> =
        safeCall {
            client.submitFormWithBinaryData(
                url = ApiConfig.apiUrl("/videos"),
                formData = formData {
                    append("title", title)
                    if (description != null) append("description", description)
                    append("categoryId", categoryId)
                    append("file", fileBytes, Headers.build {
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        append(HttpHeaders.ContentType, "video/mp4")
                    })
                }
            )
        }

    suspend fun submitVideoForReview(videoId: String): ApiResult<VideoDetailResponse> =
        safeCall { client.post(ApiConfig.apiUrl("/videos/$videoId/submit")) }

    // ── Internal helpers ────────────────────────────────────────────
    private suspend inline fun <reified T> safeCall(block: () -> HttpResponse): ApiResult<T> {
        return try {
            val response = block()
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body<T>())
            } else {
                val errorBody = try {
                    response.body<ApiErrorResponse>()
                } catch (_: Exception) {
                    null
                }
                ApiResult.Error(
                    code = errorBody?.error?.code ?: response.status.value.toString(),
                    message = errorBody?.error?.message ?: response.status.description,
                    httpStatus = response.status.value
                )
            }
        } catch (e: Exception) {
            ApiResult.NetworkError(e)
        }
    }
}
