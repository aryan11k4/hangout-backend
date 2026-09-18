package com.hangout.backend.broadcast.dto;

import java.util.UUID;

public record BroadcastParticipantDto(
        UUID userId,
        String username
) {}
