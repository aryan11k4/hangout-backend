package com.hangout.backend.group.dto;

import com.hangout.backend.common.enums.GroupVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record GroupCreateDto(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 300) String description,
        @NotNull GroupVisibility visibility
) {}