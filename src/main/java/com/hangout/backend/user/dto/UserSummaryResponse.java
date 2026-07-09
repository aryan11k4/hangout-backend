package com.hangout.backend.user.dto;

import java.util.UUID;

/** Public-safe view of a user - never includes the password hash or email. */
public record UserSummaryResponse(
        UUID id,
        String username,
        String displayName,
        String profilePicture
) {}
