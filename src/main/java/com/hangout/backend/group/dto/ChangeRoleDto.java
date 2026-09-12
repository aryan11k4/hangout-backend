package com.hangout.backend.group.dto;

import com.hangout.backend.common.enums.GroupRole;
import jakarta.validation.constraints.NotNull;

public record ChangeRoleDto(
        @NotNull GroupRole role
) {}