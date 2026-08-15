package com.scholarshiphub.service;

import com.scholarshiphub.dto.request.ScholarshipRequest;
import com.scholarshiphub.dto.response.ScholarshipResponse;
import com.scholarshiphub.entity.enums.ScholarshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScholarshipService {

    /** Public/student browsing -- always restricted to PUBLISHED scholarships. */
    Page<ScholarshipResponse> browsePublished(String search, Pageable pageable);

    /** Admin listing -- any status, optionally filtered. */
    Page<ScholarshipResponse> adminList(ScholarshipStatus status, String search, Pageable pageable);

    ScholarshipResponse getById(Long id);

    ScholarshipResponse create(ScholarshipRequest request, Long actorAdminId);

    ScholarshipResponse update(Long id, ScholarshipRequest request, Long actorAdminId);

    ScholarshipResponse updateStatus(Long id, ScholarshipStatus status, Long actorAdminId);

    void delete(Long id, Long actorAdminId);
}
