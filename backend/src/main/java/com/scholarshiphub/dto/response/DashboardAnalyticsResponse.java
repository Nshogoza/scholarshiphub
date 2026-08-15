package com.scholarshiphub.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardAnalyticsResponse(
        long totalStudents,
        long totalReviewers,
        long totalScholarships,
        long totalApplications,
        double approvalRatePercent,
        Map<String, Long> applicationsByStatus,
        List<AuditLogResponse> recentActivity
) {
}
