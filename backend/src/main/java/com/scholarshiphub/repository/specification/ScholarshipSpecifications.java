package com.scholarshiphub.repository.specification;

import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ScholarshipSpecifications {

    private ScholarshipSpecifications() {
    }

    public static Specification<Scholarship> hasStatus(ScholarshipStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Scholarship> titleContains(String search) {
        return (root, query, cb) -> (search == null || search.isBlank())
                ? null
                : cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%");
    }
}
