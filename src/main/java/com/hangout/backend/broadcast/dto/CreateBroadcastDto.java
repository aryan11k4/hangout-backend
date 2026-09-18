package com.hangout.backend.broadcast.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBroadcastDto(
        @NotBlank String videoId,
        String title,
        String thumbnailUrl
) {}