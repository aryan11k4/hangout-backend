package com.hangout.backend.group.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinRequestByCodeDto(
        @NotBlank String inviteCode
) {}