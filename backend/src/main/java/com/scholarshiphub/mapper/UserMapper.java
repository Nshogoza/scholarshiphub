package com.scholarshiphub.mapper;

import com.scholarshiphub.dto.response.UserSummaryResponse;
import com.scholarshiphub.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "role", expression = "java(user.getRole().getName())")
    @Mapping(target = "status", expression = "java(user.getStatus().name())")
    UserSummaryResponse toSummary(User user);
}
