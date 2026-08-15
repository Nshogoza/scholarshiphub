package com.scholarshiphub.dto.response;

import com.scholarshiphub.entity.enums.RoleName;
import java.time.Instant;

public record UserSummaryResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        String phone,
        RoleName role,
        String status,
        boolean emailVerified,
        Instant createdAt
) {
}
