package com.scholarshiphub.service;

import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationDocumentResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.entity.enums.ApplicationStatus;
import com.scholarshiphub.entity.enums.RoleName;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface ApplicationService {

    ApplicationDetailResponse create(Long studentId, Long scholarshipId);

    Page<ApplicationSummaryResponse> listMine(Long studentId, ApplicationStatus status, Pageable pageable);

    /** Enforces that the requester is the owning student, the assigned reviewer, or an admin. */
    ApplicationDetailResponse getDetail(Long applicationId, Long requesterId, RoleName requesterRole);

    ApplicationDocumentResponse uploadDocument(Long applicationId, Long studentId, String documentName,
                                                MultipartFile file);

    void deleteDocument(Long applicationId, Long documentId, Long studentId);

    DownloadableDocument downloadDocument(Long applicationId, Long documentId, Long requesterId,
                                           RoleName requesterRole);

    ApplicationDetailResponse submit(Long applicationId, Long studentId);

    Page<ApplicationSummaryResponse> listForReviewer(Long reviewerId, ApplicationStatus status, Pageable pageable);

    Page<ApplicationSummaryResponse> adminList(ApplicationStatus status, Long scholarshipId, Pageable pageable);

    ApplicationSummaryResponse assignReviewer(Long applicationId, Long reviewerId, Long actorAdminId);
}
