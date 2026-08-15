package com.scholarshiphub.mapper;

import com.scholarshiphub.dto.response.ApplicationDetailResponse;
import com.scholarshiphub.dto.response.ApplicationDocumentResponse;
import com.scholarshiphub.dto.response.ApplicationSummaryResponse;
import com.scholarshiphub.dto.response.ReviewResponse;
import com.scholarshiphub.entity.Application;
import com.scholarshiphub.entity.ApplicationDocument;
import com.scholarshiphub.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ScholarshipMapper.class})
public interface ApplicationMapper {

    @Mapping(target = "scholarshipId", expression = "java(application.getScholarship().getId())")
    @Mapping(target = "scholarshipTitle", expression = "java(application.getScholarship().getTitle())")
    @Mapping(target = "studentId", expression = "java(application.getStudent().getId())")
    @Mapping(target = "studentName", expression = "java(application.getStudent().getFullName())")
    @Mapping(target = "reviewerId",
            expression = "java(application.getReviewer() != null ? application.getReviewer().getId() : null)")
    @Mapping(target = "reviewerName",
            expression = "java(application.getReviewer() != null ? application.getReviewer().getFullName() : null)")
    ApplicationSummaryResponse toSummary(Application application);

    @Mapping(target = "scholarship", source = "scholarship")
    @Mapping(target = "studentId", expression = "java(application.getStudent().getId())")
    @Mapping(target = "studentName", expression = "java(application.getStudent().getFullName())")
    @Mapping(target = "reviewerId",
            expression = "java(application.getReviewer() != null ? application.getReviewer().getId() : null)")
    @Mapping(target = "reviewerName",
            expression = "java(application.getReviewer() != null ? application.getReviewer().getFullName() : null)")
    ApplicationDetailResponse toDetail(Application application);

    ApplicationDocumentResponse toDocumentResponse(ApplicationDocument document);

    @Mapping(target = "reviewerId", expression = "java(review.getReviewer().getId())")
    @Mapping(target = "reviewerName", expression = "java(review.getReviewer().getFullName())")
    ReviewResponse toReviewResponse(Review review);
}
