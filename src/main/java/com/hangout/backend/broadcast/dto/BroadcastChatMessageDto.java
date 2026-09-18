package com.hangout.backend.broadcast.dto;

import java.time.Instant;
import java.util.UUID;

public record BroadcastChatMessageDto(
        UUID id,
        UUID broadcastId,
        UUID senderId,
        String senderUsername,
        String content,
        Instant createdAt
) {}
