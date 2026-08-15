package com.scholarshiphub.mapper;

import com.scholarshiphub.dto.response.StudentProfileResponse;
import com.scholarshiphub.entity.StudentProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StudentProfileMapper {

    @Mapping(target = "userId", expression = "java(profile.getUser().getId())")
    StudentProfileResponse toResponse(StudentProfile profile);
}
