package com.scholarshiphub.repository.specification;

import com.scholarshiphub.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

public final class AuditLogSpecifications {

    private AuditLogSpecifications() {
    }

    public static Specification<AuditLog> actionEquals(String action) {
        return (root, query, cb) -> (action == null || action.isBlank())
                ? null
                : cb.equal(root.get("action"), action);
    }

    public static Specification<AuditLog> actorIs(Long actorUserId) {
        return (root, query, cb) -> actorUserId == null
                ? null
                : cb.equal(root.get("actorUser").get("id"), actorUserId);
    }
}
