package com.cognia.app.service

import com.cognia.app.config.AppConfig
import com.cognia.app.database.VideosTable
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
    private val config: AppConfig
) : VideoProcessingService {

    private val logger = LoggerFactory.getLogger(FfmpegVideoProcessingService::class.java)

    override suspend fun processVideo(videoId: String, rawFilePath: String) {
        withContext(Dispatchers.IO) {
            try {
                updateVideoStatus(videoId, "PROCESSING")

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
                    return@withContext
                }

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

                logger.info("Video processing completed for $videoId")

            } catch (e: Exception) {
                logger.error("Video processing failed for $videoId", e)
                updateVideoStatus(videoId, "PROCESSING_FAILED")
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
}
