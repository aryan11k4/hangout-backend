package com.hangout.backend.notification.dto;

public enum NotificationType {
    JOIN_REQUEST_RECEIVED,   // sent to OWNER/ADMIN when someone requests to join
    JOIN_REQUEST_APPROVED,   // sent to the requester
    JOIN_REQUEST_REJECTED    // sent to the requester
}