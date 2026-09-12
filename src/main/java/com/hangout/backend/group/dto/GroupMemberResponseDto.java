package com.hangout.backend.group.dto;

import com.hangout.backend.common.enums.GroupRole;

import java.time.Instant;
import java.util.UUID;

public record GroupMemberResponseDto(
        UUID userId,
        String username,
        GroupRole role,
        Instant joinedAt
) {}