package com.hangout.backend.notification.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Ephemeral, WS-only - never persisted. If the recipient isn't connected
 * when this is sent, it's simply lost (per product decision - no
 * notifications table for MVP).
 */
public record NotificationDto(
        NotificationType type,
        String message,
        UUID groupId,
        String groupName,
        UUID actorId,
        String actorUsername,
        Instant timestamp
) {}