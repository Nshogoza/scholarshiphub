package com.scholarshiphub.service.impl;

import com.scholarshiphub.dto.response.DashboardAnalyticsResponse;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.mapper.AuditLogMapper;
import com.scholarshiphub.repository.ApplicationRepository;
import com.scholarshiphub.repository.AuditLogRepository;
import com.scholarshiphub.repository.ScholarshipRepository;
import com.scholarshiphub.repository.UserRepository;
import com.scholarshiphub.service.AnalyticsService;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final int RECENT_ACTIVITY_LIMIT = 15;

    private final UserRepository userRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final ApplicationRepository applicationRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics() {
        long totalStudents = userRepository.countByRole_Name(RoleName.STUDENT);
        long totalReviewers = userRepository.countByRole_Name(RoleName.REVIEWER);
        long totalScholarships = scholarshipRepository.count();
        long totalApplications = applicationRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            byStatus.put(status.name(), applicationRepository.countByStatus(status));
        }

        long approved = byStatus.get(ApplicationStatus.APPROVED.name());
        long rejected = byStatus.get(ApplicationStatus.REJECTED.name());
        long decided = approved + rejected;
        double approvalRate = decided == 0 ? 0.0 : (approved * 100.0) / decided;

        var recentActivity = auditLogRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_ACTIVITY_LIMIT, Sort.by("createdAt").descending()))
                .map(auditLogMapper::toResponse)
                .getContent();

        return new DashboardAnalyticsResponse(
                totalStudents, totalReviewers, totalScholarships, totalApplications,
                Math.round(approvalRate * 100) / 100.0, byStatus, recentActivity);
    }
}
