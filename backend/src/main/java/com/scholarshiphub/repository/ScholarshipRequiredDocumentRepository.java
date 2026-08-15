package com.scholarshiphub.repository;

import com.scholarshiphub.entity.ScholarshipRequiredDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScholarshipRequiredDocumentRepository extends JpaRepository<ScholarshipRequiredDocument, Long> {

    List<ScholarshipRequiredDocument> findAllByScholarship_Id(Long scholarshipId);
}
