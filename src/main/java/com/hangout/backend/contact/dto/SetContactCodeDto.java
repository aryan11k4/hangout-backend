package com.hangout.backend.contact.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SetContactCodeDto(
        @NotBlank
        @Pattern(regexp = "\\d{3}", message = "Code must be exactly 3 digits (000-999)")
        String code
) {}