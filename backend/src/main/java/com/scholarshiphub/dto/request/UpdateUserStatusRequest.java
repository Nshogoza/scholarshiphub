package com.scholarshiphub.dto.request;

import com.scholarshiphub.entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(

        @NotNull(message = "Status is required")
        UserStatus status
) {
}
