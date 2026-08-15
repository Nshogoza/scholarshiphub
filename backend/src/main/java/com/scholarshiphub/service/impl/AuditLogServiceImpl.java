package com.scholarshiphub.service.impl;

import com.scholarshiphub.entity.AuditLog;
import com.scholarshiphub.repository.AuditLogRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    /**
     * REQUIRES_NEW so an audit entry for a failed operation (e.g. a failed
     * login) is still committed even when the enclosing transaction that
     * triggered it is rolled back.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long actorUserId, String action, String entityType, Long entityId, String details) {
        String ip = currentClientIp();

        AuditLog.AuditLogBuilder builder = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .ipAddress(ip);

        if (actorUserId != null) {
            userRepository.findById(actorUserId).ifPresent(builder::actorUser);
        }

        auditLogRepository.save(builder.build());
        auditLog.info("action={} entityType={} entityId={} actorUserId={} ip={} details={}",
                action, entityType, entityId, actorUserId, ip, details);
    }

    private String currentClientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
