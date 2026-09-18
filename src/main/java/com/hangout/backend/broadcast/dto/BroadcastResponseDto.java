package com.hangout.backend.broadcast.dto;

import com.hangout.backend.common.enums.BroadcastStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Covers both "get broadcast info" and "get current sync state" - serverTime
 * is always included so the client can compute live position via
 * currentPosition + (now - serverTime) when isPlaying is true, per the
 * clock-sync approach in the workflow spec.
 */
public record BroadcastResponseDto(
        UUID broadcastId,
        UUID groupId,
        UUID hostId,
        String hostUsername,
        String videoId,
        String title,
        String thumbnailUrl,
        BroadcastStatus status,
        double currentPosition,
        boolean isPlaying,
        long viewerCount,
        Instant serverTime,
        Instant createdAt
) {}