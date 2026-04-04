package com.cognia.app.service

import com.cognia.app.dto.moderation.AuditLogEntry
import com.cognia.app.repository.AuditLogRepository

class AuditLogService(
    private val auditLogRepository: AuditLogRepository
) {

    fun log(moderatorId: String, action: String, targetType: String, targetId: String, details: String? = null) {
        auditLogRepository.log(moderatorId, action, targetType, targetId, details)
    }

    fun getEntries(limit: Int = 100, offset: Long = 0): List<AuditLogEntry> {
        return auditLogRepository.getEntries(limit, offset).map { it.toDto() }
    }

    fun getByModerator(moderatorId: String, limit: Int = 100): List<AuditLogEntry> {
        return auditLogRepository.getByModerator(moderatorId, limit).map { it.toDto() }
    }

    private fun AuditLogRepository.AuditLogRow.toDto() = AuditLogEntry(
        id = id,
        moderatorId = moderatorId,
        action = action,
        targetType = targetType,
        targetId = targetId,
        details = details,
        createdAt = createdAt
    )
}
