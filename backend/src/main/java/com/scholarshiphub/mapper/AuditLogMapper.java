package com.scholarshiphub.mapper;

import com.scholarshiphub.dto.response.AuditLogResponse;
import com.scholarshiphub.entity.AuditLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(target = "actorUserId", expression = "java(log.getActorUser() != null ? log.getActorUser().getId() : null)")
    @Mapping(target = "actorEmail", expression = "java(log.getActorUser() != null ? log.getActorUser().getEmail() : \"system\")")
    AuditLogResponse toResponse(AuditLog log);
}
