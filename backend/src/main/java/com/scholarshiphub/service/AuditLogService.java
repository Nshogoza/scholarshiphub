package com.scholarshiphub.service;

/** Records security- and business-relevant events (logins, failed logins,
 *  submissions, review decisions, admin actions) to both the durable
 *  {@code audit_logs} table and the dedicated AUDIT log stream. */
public interface AuditLogService {

    void record(Long actorUserId, String action, String entityType, Long entityId, String details);
}
