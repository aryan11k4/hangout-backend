package com.hangout.backend.group.dto;

import com.hangout.backend.common.enums.JoinRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record JoinRequestResponseDto(
        UUID requestId,
        UUID groupId,
        UUID userId,
        String username,
        JoinRequestStatus status,
        Instant requestedAt
) {}