package com.hangout.backend.broadcast.dto;

import com.hangout.backend.common.enums.PlaybackEventType;

public record ViewerEventDto(
        PlaybackEventType type,   // VIEWER_JOINED or VIEWER_LEFT
        String username,
        long viewerCount
) {}
