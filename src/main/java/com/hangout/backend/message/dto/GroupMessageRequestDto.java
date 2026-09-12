package com.hangout.backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record GroupMessageRequestDto(
        @NotNull UUID groupId,
        @NotBlank @Size(max = 2000) String content
) {}