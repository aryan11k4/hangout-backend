package com.hangout.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPasswordDto(
        @NotBlank @Size(min = 8, max = 100) String newPassword
) {}