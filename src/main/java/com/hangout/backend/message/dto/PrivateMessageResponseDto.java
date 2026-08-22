package com.hangout.backend.message.dto;

import com.hangout.backend.common.enums.MessageType;

import java.time.Instant;
import java.util.UUID;

public record PrivateMessageResponseDto(
        UUID id,
        UUID senderId,
        UUID receiverId,
        String content,
        MessageType messageType,
        Instant createdAt,
        boolean edited
) {}