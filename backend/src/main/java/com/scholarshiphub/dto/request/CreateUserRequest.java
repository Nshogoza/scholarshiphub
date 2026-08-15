package com.scholarshiphub.dto.request;

import com.scholarshiphub.entity.enums.RoleName;
import com.scholarshiphub.validation.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Used by an administrator to provision REVIEWER or ADMIN accounts --
 *  public self-registration only ever creates STUDENT accounts. */
public record CreateUserRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @StrongPassword
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @NotBlank
        @Size(max = 100)
        String lastName,

        @Pattern(regexp = "^$|^[+0-9()\\-\\s]{7,20}$", message = "Phone number format is invalid")
        String phone,

        @NotNull(message = "Role is required")
        RoleName role
) {
}
