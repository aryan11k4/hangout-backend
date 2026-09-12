package com.hangout.backend.group.dto;

import com.hangout.backend.common.enums.GroupVisibility;

import java.time.Instant;
import java.util.UUID;

public record GroupResponseDto(
        UUID id,
        String name,
        String description,
        GroupVisibility visibility,
        String inviteCode,
        UUID ownerId,
        String ownerUsername,
        Instant createdAt
) {}