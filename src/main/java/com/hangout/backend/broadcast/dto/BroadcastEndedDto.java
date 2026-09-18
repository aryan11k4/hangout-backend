package com.hangout.backend.broadcast.dto;

import java.util.UUID;

public record BroadcastEndedDto(
        UUID broadcastId,
        String reason  // "HOST_ENDED" or "HOST_DISCONNECTED"
) {}
