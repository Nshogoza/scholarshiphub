package com.scholarshiphub.repository;

import com.scholarshiphub.entity.Scholarship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * {@link JpaSpecificationExecutor} backs the dynamic filter/sort/paginate
 * browsing endpoint (title search, status, deadline range) without a
 * combinatorial explosion of derived query methods.
 */
public interface ScholarshipRepository extends JpaRepository<Scholarship, Long>,
        JpaSpecificationExecutor<Scholarship> {
}
