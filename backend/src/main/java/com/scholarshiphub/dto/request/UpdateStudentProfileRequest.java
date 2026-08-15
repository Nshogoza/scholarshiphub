package com.scholarshiphub.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateStudentProfileRequest(

        @Size(max = 50)
        String educationLevel,

        @Size(max = 255)
        String school,

        @DecimalMin(value = "0.0", message = "GPA cannot be negative")
        @DecimalMax(value = "10.0", message = "GPA cannot exceed 10.0")
        BigDecimal gpa,

        @Size(max = 5000, message = "Personal statement cannot exceed 5000 characters")
        String personalStatement
) {
}
