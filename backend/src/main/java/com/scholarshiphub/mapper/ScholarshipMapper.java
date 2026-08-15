package com.scholarshiphub.mapper;

import com.scholarshiphub.dto.response.RequiredDocumentResponse;
import com.scholarshiphub.dto.response.ScholarshipResponse;
import com.scholarshiphub.entity.Scholarship;
import com.scholarshiphub.entity.ScholarshipRequiredDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ScholarshipMapper {

    @Mapping(target = "createdByName", expression = "java(scholarship.getCreatedBy().getFullName())")
    ScholarshipResponse toResponse(Scholarship scholarship);

    RequiredDocumentResponse toDocumentResponse(ScholarshipRequiredDocument document);
}
