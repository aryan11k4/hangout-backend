package com.hangout.backend.broadcast.dto;

import java.util.UUID;

public record BroadcastSummaryDto(
        UUID broadcastId,
        UUID hostId,
        String hostUsername,
        String videoId,
        String title,
        String thumbnailUrl,
        long viewerCount
) {}