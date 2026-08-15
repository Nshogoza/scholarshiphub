package com.scholarshiphub.repository.specification;

import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ApplicationSpecifications {

    private ApplicationSpecifications() {
    }

    public static Specification<Application> hasStatus(ApplicationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Application> forScholarship(Long scholarshipId) {
        return (root, query, cb) -> scholarshipId == null
                ? null
                : cb.equal(root.get("scholarship").get("id"), scholarshipId);
    }

    public static Specification<Application> forReviewer(Long reviewerId) {
        return (root, query, cb) -> reviewerId == null
                ? null
                : cb.equal(root.get("reviewer").get("id"), reviewerId);
    }

    public static Specification<Application> forStudent(Long studentId) {
        return (root, query, cb) -> studentId == null
                ? null
                : cb.equal(root.get("student").get("id"), studentId);
    }
}
