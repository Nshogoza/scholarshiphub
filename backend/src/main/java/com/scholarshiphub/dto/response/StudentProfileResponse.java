package com.scholarshiphub.dto.response;

import java.math.BigDecimal;

public record StudentProfileResponse(
        Long userId,
        String educationLevel,
        String school,
        BigDecimal gpa,
        String personalStatement
) {
}
