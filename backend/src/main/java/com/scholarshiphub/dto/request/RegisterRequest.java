package com.scholarshiphub.dto.request;

import com.scholarshiphub.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Public self-registration always creates a STUDENT account; reviewer and
 *  admin accounts are provisioned by an administrator (see UserManagement). */
public record RegisterRequest(

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Password is required")
        @StrongPassword
        String password,

        @NotBlank(message = "First name is required")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,20}$", message = "Phone number format is invalid")
        String phone
) {
}
