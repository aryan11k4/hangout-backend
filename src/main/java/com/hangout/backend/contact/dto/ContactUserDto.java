package com.hangout.backend.contact.dto;

import java.util.UUID;

/**
 * Never includes email/password - this is what gets shown to OTHER users
 * during search, in contact lists, and in request lists.
 */
public record ContactUserDto(
        UUID id,
        String username,
        String code
) {}