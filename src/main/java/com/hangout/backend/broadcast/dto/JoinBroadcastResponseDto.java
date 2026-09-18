package com.hangout.backend.broadcast.dto;

import java.time.Instant;
import java.util.UUID;

public record JoinBroadcastResponseDto(
        UUID broadcastId,
        double currentPosition,
        boolean isPlaying,
        Instant serverTime
) {}