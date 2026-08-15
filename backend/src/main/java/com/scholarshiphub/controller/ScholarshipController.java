package com.scholarshiphub.controller;

import com.scholarshiphub.dto.response.ApiResponse;
import com.scholarshiphub.dto.response.PageResponse;
import com.scholarshiphub.dto.response.ScholarshipResponse;
import com.scholarshiphub.service.ScholarshipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/scholarships")
@RequiredArgsConstructor
@Tag(name = "Scholarships", description = "Browse published scholarships")
public class ScholarshipController {

    private final ScholarshipService scholarshipService;

    @GetMapping
    @Operation(summary = "Browse published, currently-open scholarships (search + pagination)")
    public ResponseEntity<ApiResponse<PageResponse<ScholarshipResponse>>> browse(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "applicationDeadline") Pageable pageable) {
        var page = scholarshipService.browsePublished(search, pageable);
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(page)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get scholarship details by id")
    public ResponseEntity<ApiResponse<ScholarshipResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(scholarshipService.getById(id)));
    }
}
