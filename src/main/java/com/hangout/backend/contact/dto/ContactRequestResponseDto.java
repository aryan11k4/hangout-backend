package com.hangout.backend.contact.dto;

import com.hangout.backend.common.enums.ContactRequestStatus;

import java.time.Instant;
import java.util.UUID;

public record ContactRequestResponseDto(
        UUID requestId,
        ContactUserDto sender,
        ContactRequestStatus status,
        Instant createdAt
) {}