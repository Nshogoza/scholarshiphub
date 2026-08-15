package com.scholarshiphub.service;

import com.scholarshiphub.dto.response.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** Admin-facing read side of the audit trail (writing is done internally via {@link AuditLogService}). */
public interface AuditLogQueryService {

    Page<AuditLogResponse> search(String action, Long actorUserId, Pageable pageable);
}
