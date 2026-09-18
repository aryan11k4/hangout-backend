package com.hangout.backend.broadcast.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BroadcastChatSendDto(
        @NotBlank @Size(max = 1000) String content
) {}
