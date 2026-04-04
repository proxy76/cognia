package com.cognia.app.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory

class VideoProcessingQueue(
    private val processingService: VideoProcessingService
) {
    private val logger = LoggerFactory.getLogger(VideoProcessingQueue::class.java)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun enqueue(videoId: String, rawFilePath: String) {
        logger.info("Enqueueing video $videoId for processing")
        scope.launch {
            processingService.processVideo(videoId, rawFilePath)
        }
    }

    fun shutdown() {
        logger.info("Shutting down video processing queue")
        scope.cancel()
    }
}
