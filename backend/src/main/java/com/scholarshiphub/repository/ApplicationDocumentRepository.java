package com.scholarshiphub.repository;

import com.scholarshiphub.entity.ApplicationDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationDocumentRepository extends JpaRepository<ApplicationDocument, Long> {

    List<ApplicationDocument> findAllByApplication_Id(Long applicationId);
}
