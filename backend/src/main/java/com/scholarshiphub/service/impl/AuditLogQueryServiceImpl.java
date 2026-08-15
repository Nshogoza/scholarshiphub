package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.response.AuditLogResponse;
import com.scholarshiphub.entity.AuditLog;
import com.scholarshiphub.mapper.AuditLogMapper;
import com.scholarshiphub.repository.AuditLogRepository;
import com.scholarshiphub.repository.specification.AuditLogSpecifications;
import com.scholarshiphub.service.AuditLogQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogQueryServiceImpl implements AuditLogQueryService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> search(String action, Long actorUserId, Pageable pageable) {
        Specification<AuditLog> spec = Specification.where(AuditLogSpecifications.actionEquals(action))
                .and(AuditLogSpecifications.actorIs(actorUserId));
        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toResponse);
    }
}
