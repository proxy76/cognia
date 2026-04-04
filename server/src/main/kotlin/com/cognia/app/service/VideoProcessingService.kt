package com.cognia.app.service

import com.cognia.app.config.AppConfig
import com.cognia.app.database.VideosTable
import com.cognia.app.repository.ModerationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.io.File

interface VideoProcessingService {
    suspend fun processVideo(videoId: String, rawFilePath: String)
}

class FfmpegVideoProcessingService(
    private val config: AppConfig,
    private val moderationRepository: ModerationRepository? = null
) : VideoProcessingService {

    private val logger = LoggerFactory.getLogger(FfmpegVideoProcessingService::class.java)

    override suspend fun processVideo(videoId: String, rawFilePath: String) {
        withContext(Dispatchers.IO) {
            try {
                updateVideoStatus(videoId, "PROCESSING")
                updateProcessingStatus(videoId, "PROCESSING")

                val processedDir = File(config.video.processedPath)
                val thumbnailDir = File(config.video.thumbnailPath)
                processedDir.mkdirs()
                thumbnailDir.mkdirs()

                val processedPath = "${config.video.processedPath}/$videoId.mp4"
                val thumbnailPath = "${config.video.thumbnailPath}/$videoId.jpg"

                // Transcode video with FFmpeg
                val transcodeResult = runFfmpeg(
                    config.ffmpeg.path,
                    "-i", rawFilePath,
                    "-c:v", "libx264",
                    "-preset", "fast",
                    "-crf", "23",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    "-y", processedPath
                )

                if (transcodeResult != 0) {
                    logger.error("FFmpeg transcoding failed for video $videoId with exit code $transcodeResult")
                    updateVideoStatus(videoId, "PROCESSING_FAILED")
                    updateProcessingStatus(videoId, "FAILED", "FFmpeg transcoding failed with exit code $transcodeResult")
                    return@withContext
                }

                // Extract duration via ffprobe
                val duration = probeDuration(rawFilePath)

                // Generate thumbnail
                val thumbnailResult = runFfmpeg(
                    config.ffmpeg.path,
                    "-i", processedPath,
                    "-ss", "00:00:01",
                    "-vframes", "1",
                    "-vf", "scale=360:-1",
                    "-y", thumbnailPath
                )

                if (thumbnailResult != 0) {
                    logger.warn("Thumbnail generation failed for video $videoId, continuing without thumbnail")
                }

                // Update video record with processed paths
                updateVideoRecord(
                    videoId,
                    processedPath,
                    if (thumbnailResult == 0) thumbnailPath else null
                )

                updateProcessingStatus(videoId, "COMPLETED", durationSeconds = duration)

                // Auto-submit for moderation review
                updateVideoStatus(videoId, "PENDING_REVIEW")
                moderationRepository?.createReview(videoId, "VIDEO", isPostPublication = false)

                logger.info("Video processing completed and submitted for review: $videoId")

            } catch (e: Exception) {
                logger.error("Video processing failed for $videoId", e)
                updateVideoStatus(videoId, "PROCESSING_FAILED")
                updateProcessingStatus(videoId, "FAILED", e.message)
            }
        }
    }

    internal fun runFfmpeg(vararg args: String): Int {
        val process = ProcessBuilder(*args)
            .redirectErrorStream(true)
            .start()
        process.inputStream.bufferedReader().readLines() // consume output
        return process.waitFor()
    }

    internal fun probeDuration(filePath: String): Double? {
        return try {
            val ffprobePath = config.ffmpeg.path.replace("ffmpeg", "ffprobe")
            val process = ProcessBuilder(
                ffprobePath,
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                filePath
            ).redirectErrorStream(true).start()

            val output = process.inputStream.bufferedReader().readText().trim()
            process.waitFor()
            output.toDoubleOrNull()
        } catch (e: Exception) {
            logger.warn("Failed to probe duration for $filePath", e)
            null
        }
    }

    internal fun updateVideoRecord(videoId: String, videoUrl: String, thumbnailUrl: String?) {
        transaction {
            VideosTable.update({ VideosTable.id eq videoId }) {
                it[VideosTable.videoUrl] = videoUrl
                if (thumbnailUrl != null) {
                    it[VideosTable.thumbnailUrl] = thumbnailUrl
                }
                it[VideosTable.status] = "PROCESSED"
            }
        }
    }

    internal fun updateVideoStatus(videoId: String, status: String) {
        transaction {
            VideosTable.update({ VideosTable.id eq videoId }) {
                it[VideosTable.status] = status
            }
        }
    }

    internal fun updateProcessingStatus(videoId: String, processingStatus: String, error: String? = null, durationSeconds: Double? = null) {
        transaction {
            VideosTable.update({ VideosTable.id eq videoId }) {
                it[VideosTable.processingStatus] = processingStatus
                it[VideosTable.processingError] = error
                if (durationSeconds != null) {
                    it[VideosTable.durationSeconds] = durationSeconds
                }
            }
        }
    }
}
