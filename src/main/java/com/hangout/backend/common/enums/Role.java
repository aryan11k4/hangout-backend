package com.hangout.backend.common.enums;

/**
 * Application-wide (system) role. This is NOT the same as a group role
 * (see {@link GroupRole}) - it only governs app-level permissions such as
 * access to a future moderation/admin panel.
 */
public enum Role {
    USER,
    ADMIN
}
