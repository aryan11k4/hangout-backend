package com.hangout.backend.auth.dto;

import com.hangout.backend.user.dto.UserSummaryResponse;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UserSummaryResponse user
) {
    public static AuthResponse of(String accessToken, UserSummaryResponse user) {
        return new AuthResponse(accessToken, "Bearer", user);
    }
}
