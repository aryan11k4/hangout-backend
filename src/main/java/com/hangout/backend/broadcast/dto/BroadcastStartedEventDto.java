package com.hangout.backend.broadcast.dto;

import java.util.UUID;

/**
 * Published to /topic/group.{groupId}.broadcasts the moment a broadcast is
 * created - lets clients on the group screen (not yet inside any specific
 * broadcast) update their active-broadcasts list live. Same field shape as
 * BroadcastSummaryDto plus a "type" discriminator, so the frontend can
 * merge this straight into wherever it caches that list.
 */
public record BroadcastStartedEventDto(
        String type, // always "BROADCAST_STARTED"
        UUID broadcastId,
        UUID hostId,
        String hostUsername,
        String videoId,
        String title,
        String thumbnailUrl,
        long viewerCount
) {
    public static BroadcastStartedEventDto of(BroadcastSummaryDto summary) {
        return new BroadcastStartedEventDto(
                "BROADCAST_STARTED",
                summary.broadcastId(),
                summary.hostId(),
                summary.hostUsername(),
                summary.videoId(),
                summary.title(),
                summary.thumbnailUrl(),
                summary.viewerCount()
        );
    }
}