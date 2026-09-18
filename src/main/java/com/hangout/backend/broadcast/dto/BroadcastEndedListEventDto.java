package com.hangout.backend.broadcast.dto;

import java.util.UUID;

/**
 * Published to /topic/group.{groupId}.broadcasts when a broadcast ends -
 * lets clients on the group screen remove it from their list immediately,
 * without needing to have ever subscribed to that broadcast's own topic.
 * Deliberately a distinct, lighter event from BroadcastEndedDto (which
 * still serves /topic/broadcast.{broadcastId} for people actually inside
 * the broadcast) - this one only carries what a list view needs.
 */
public record BroadcastEndedListEventDto(
        String type, // always "BROADCAST_ENDED_FROM_LIST"
        UUID broadcastId
) {
    public static BroadcastEndedListEventDto of(UUID broadcastId) {
        return new BroadcastEndedListEventDto("BROADCAST_ENDED_FROM_LIST", broadcastId);
    }
}