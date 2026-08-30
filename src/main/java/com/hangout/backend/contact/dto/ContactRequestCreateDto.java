package com.hangout.backend.contact.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ContactRequestCreateDto(
        @NotNull UUID receiverId
) {}