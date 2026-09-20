package com.hangout.backend.broadcast.dto;

import com.hangout.backend.common.enums.PlaybackEventType;

import java.time.Instant;
import java.util.UUID;

/**
 * type is always PlaybackEventType.CHAT_MESSAGE - required because the
 * frontend's live WS event handler dispatches purely on event.type across
 * every payload delivered on /topic/broadcast.{broadcastId} (PLAY, PAUSE,
 * SEEK, VIEWER_JOINED, VIEWER_LEFT, BROADCAST_ENDED all carry it). Without
 * this field, a chat message arrives with type === undefined and matches
 * none of the frontend's dispatch branches, so it's silently dropped even
 * though it was sent and persisted correctly - this was the actual cause
 * of "chat messages only appear after reload" (reload uses a separate REST
 * path, GET /api/broadcasts/{broadcastId}/messages, unaffected by this).
 * <p>
 * Used for BOTH the live WS payload (sendMessage) and the REST history
 * response (getHistory) - the extra field is harmless on the REST side
 * even though the frontend's history type doesn't read it, and keeping
 * both paths building the exact same DTO avoids a second constructor.
 */
public record BroadcastChatMessageDto(
        PlaybackEventType type,
        UUID id,
        UUID broadcastId,
        UUID senderId,
        String senderUsername,
        String content,
        Instant createdAt
) {
    public static BroadcastChatMessageDto of(UUID id, UUID broadcastId, UUID senderId,
                                             String senderUsername, String content, Instant createdAt) {
        return new BroadcastChatMessageDto(
                PlaybackEventType.CHAT_MESSAGE, id, broadcastId, senderId, senderUsername, content, createdAt
        );
    }
}