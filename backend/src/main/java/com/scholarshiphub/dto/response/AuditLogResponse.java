package com.scholarshiphub.dto.response;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        Long actorUserId,
        String actorEmail,
        String action,
        String entityType,
        Long entityId,
        String details,
        String ipAddress,
        Instant createdAt
) {
}
