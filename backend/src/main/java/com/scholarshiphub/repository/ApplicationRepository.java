package com.scholarshiphub.repository;

import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApplicationRepository extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    Optional<Application> findByScholarship_IdAndStudent_Id(Long scholarshipId, Long studentId);

    Page<Application> findAllByStudent_Id(Long studentId, Pageable pageable);

    Page<Application> findAllByReviewer_Id(Long reviewerId, Pageable pageable);

    long countByStatus(ApplicationStatus status);

    long countByScholarship_Id(Long scholarshipId);
}
