package com.cognia.app.routes

import com.cognia.app.auth.UserPrincipal
import com.cognia.app.auth.authorize
import com.cognia.app.config.AppConfig
import com.cognia.app.dto.content.VideoDetailResponse
import com.cognia.app.dto.content.VideoUpdateRequest
import com.cognia.app.dto.content.VideoUploadResponse
import com.cognia.app.service.InvalidStateTransitionException
import com.cognia.app.service.VideoAccessDeniedException
import com.cognia.app.service.VideoNotFoundException
import com.cognia.app.service.VideoService
import com.cognia.app.service.ModerationService
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File
import java.util.UUID

private val ALLOWED_EXTENSIONS = setOf("mp4", "mov", "avi", "mkv", "webm")

fun Route.videoRoutes() {
    val videoService by application.inject<VideoService>()
    val appConfig by application.inject<AppConfig>()
    val moderationService by application.inject<ModerationService>()

    route("/api/v1/videos") {
        authenticate("auth-jwt") {
            // POST / — multipart upload (creators only)
            authorize("LEARNER", "REGULAR_CREATOR", "LICENSED_CREATOR") {
                post {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val multipart = call.receiveMultipart()
                    var title = ""
                    var description: String? = null
                    var categoryId = ""
                    var filePath: String? = null
                    val videoId = UUID.randomUUID().toString()

                    try {
                        multipart.forEachPart { part ->
                            when (part) {
                                is PartData.FormItem -> {
                                    when (part.name) {
                                        "title" -> title = part.value
                                        "description" -> description = part.value
                                        "categoryId" -> categoryId = part.value
                                    }
                                }
                                is PartData.FileItem -> {
                                    val originalFileName = part.originalFileName ?: "video"
                                    val extension = originalFileName.substringAfterLast('.', "").lowercase()

                                    if (extension !in ALLOWED_EXTENSIONS) {
                                        part.dispose()
                                        throw IllegalArgumentException("Invalid file type: $extension. Allowed: ${ALLOWED_EXTENSIONS.joinToString()}")
                                    }

                                    val uploadDir = File(appConfig.video.rawPath, videoId)
                                    uploadDir.mkdirs()

                                    val destFile = File(uploadDir, originalFileName)
                                    val bytes = part.streamProvider().readBytes()

                                    val fileSizeMb = bytes.size / (1024 * 1024)
                                    if (fileSizeMb > appConfig.video.maxSizeMb) {
                                        part.dispose()
                                        throw IllegalArgumentException("File size exceeds maximum of ${appConfig.video.maxSizeMb} MB")
                                    }

                                    destFile.writeBytes(bytes)
                                    filePath = destFile.absolutePath
                                }
                                else -> {}
                            }
                            part.dispose()
                        }

                        if (title.isBlank()) {
                            return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Title is required"))
                        }
                        if (categoryId.isBlank()) {
                            return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Category ID is required"))
                        }

                        val draft = videoService.createDraft(
                            creatorId = principal.userId,
                            title = title,
                            description = description,
                            categoryId = categoryId,
                            rawFilePath = filePath
                        )

                        // Auto-route based on role:
                        // LICENSED_CREATOR / ADMIN → instant publish
                        // Everyone else             → submit for moderation review
                        if (principal.role == "LICENSED_CREATOR" || principal.role == "ADMIN") {
                            val published = videoService.publishDirect(draft.id, principal.userId, principal.role)
                            call.respond(HttpStatusCode.Created, VideoUploadResponse(
                                id = published.id,
                                status = published.status,
                                message = "Video published successfully"
                            ))
                        } else {
                            val submitted = videoService.submitForReview(draft.id, principal.userId)
                            moderationService.createReview(submitted.id, "VIDEO")
                            call.respond(HttpStatusCode.Created, VideoUploadResponse(
                                id = submitted.id,
                                status = submitted.status,
                                message = "Video submitted for review"
                            ))
                        }
                    } catch (e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest, ErrorBody(e.message ?: "Invalid request"))
                    }
                }
            }

            // GET /{id} — get video details
            get("/{id}") {
                val id = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

                val video = videoService.getVideo(id)
                if (video != null) {
                    call.respond(HttpStatusCode.OK, video)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorBody("Video not found"))
                }
            }

            // GET /my — get current user's videos
            get("/my") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                val offset = call.request.queryParameters["offset"]?.toLongOrNull() ?: 0

                val videos = videoService.getCreatorVideos(principal.userId, limit, offset)
                call.respond(HttpStatusCode.OK, videos)
            }

            // PUT /{id} — update metadata (owner only)
            put("/{id}") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@put call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val id = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

                try {
                    val request = call.receive<VideoUpdateRequest>()
                    val video = videoService.updateMetadata(id, principal.userId, request)
                    call.respond(HttpStatusCode.OK, video)
                } catch (e: VideoNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Video not found"))
                } catch (e: VideoAccessDeniedException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                }
            }

            // POST /{id}/submit — submit for review
            post("/{id}/submit") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                val id = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

                try {
                    val video = videoService.submitForReview(id, principal.userId)
                    call.respond(HttpStatusCode.OK, video)
                } catch (e: VideoNotFoundException) {
                    call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Video not found"))
                } catch (e: VideoAccessDeniedException) {
                    call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                } catch (e: InvalidStateTransitionException) {
                    call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Invalid state transition"))
                }
            }

            // POST /{id}/publish — direct publish (licensed only)
            authorize("LICENSED_CREATOR") {
                post("/{id}/publish") {
                    val principal = call.principal<UserPrincipal>()
                        ?: return@post call.respond(HttpStatusCode.Unauthorized, ErrorBody("Not authenticated"))

                    val id = call.parameters["id"]
                        ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorBody("Missing video ID"))

                    try {
                        val video = videoService.publishDirect(id, principal.userId, principal.role)
                        call.respond(HttpStatusCode.OK, video)
                    } catch (e: VideoNotFoundException) {
                        call.respond(HttpStatusCode.NotFound, ErrorBody(e.message ?: "Video not found"))
                    } catch (e: VideoAccessDeniedException) {
                        call.respond(HttpStatusCode.Forbidden, ErrorBody(e.message ?: "Access denied"))
                    } catch (e: InvalidStateTransitionException) {
                        call.respond(HttpStatusCode.Conflict, ErrorBody(e.message ?: "Invalid state transition"))
                    }
                }
            }
        }
    }
}
