package com.hangout.backend.broadcast.dto;

import com.hangout.backend.common.enums.PlaybackEventType;

public record PlaybackEventDto(
        PlaybackEventType type,
        double position,
        long serverTimestampMs
) {}
