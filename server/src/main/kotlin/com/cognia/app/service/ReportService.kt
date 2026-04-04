package com.cognia.app.service

import com.cognia.app.dto.moderation.ReportResponse
import com.cognia.app.dto.moderation.ReportStatsResponse
import com.cognia.app.repository.ModerationRepository
import com.cognia.app.repository.ReportRepository

class ReportService(
    private val reportRepository: ReportRepository,
    private val moderationRepository: ModerationRepository
) {

    fun reportContent(reporterId: String, contentId: String, contentType: String, reason: String): ReportResponse {
        if (contentType !in setOf("VIDEO", "QUIZ")) {
            throw IllegalArgumentException("Content type must be VIDEO or QUIZ")
        }

        val report = reportRepository.createReport(reporterId, contentId, contentType, reason)

        // Automatically create a moderation review for the reported content (post-publication)
        // Only create if this is the first report (reportCount == 1 means new report row)
        if (report.reportCount == 1) {
            moderationRepository.createReview(contentId, contentType, isPostPublication = true)
        }

        return report.toResponse()
    }

    fun getReports(status: String? = null): List<ReportResponse> {
        return reportRepository.getReports(status).map { it.toResponse() }
    }

    fun getReportById(id: String): ReportResponse? {
        return reportRepository.findById(id)?.toResponse()
    }

    fun updateReportStatus(id: String, status: String): ReportResponse {
        val updated = reportRepository.updateStatus(id, status)
            ?: throw ReportNotFoundException("Report not found: $id")
        return updated.toResponse()
    }

    fun resolveReport(id: String, status: String, resolution: String?): ReportResponse {
        if (status !in setOf("REVIEWED", "DISMISSED")) {
            throw IllegalArgumentException("Resolution status must be REVIEWED or DISMISSED")
        }
        val updated = reportRepository.resolve(id, status, resolution)
            ?: throw ReportNotFoundException("Report not found: $id")
        return updated.toResponse()
    }

    fun getReportStats(): ReportStatsResponse {
        val stats = reportRepository.getReportStats()
        return ReportStatsResponse(
            pending = stats.pending,
            reviewed = stats.reviewed,
            dismissed = stats.dismissed,
            byContentType = stats.byContentType
        )
    }

    fun updateAiAssessment(id: String, assessment: String, confidence: String): ReportResponse {
        val updated = reportRepository.updateAiAssessment(id, assessment, confidence)
            ?: throw ReportNotFoundException("Report not found: $id")
        return updated.toResponse()
    }

    private fun ReportRepository.ReportRow.toResponse() = ReportResponse(
        id = id,
        reporterId = reporterId,
        contentId = contentId,
        contentType = contentType,
        reason = reason,
        status = status,
        createdAt = createdAt
    )
}

class ReportNotFoundException(message: String) : RuntimeException(message)
