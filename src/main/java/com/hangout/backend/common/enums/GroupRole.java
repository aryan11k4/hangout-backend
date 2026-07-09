package com.hangout.backend.common.enums;

import com.hangout.backend.group.entity.GroupMember;
import com.hangout.backend.user.entity.User;

/**
 * A user's role within a specific group. Deliberately NOT stored on the
 * {@link User} entity - the same user can be OWNER of one group, ADMIN of
 * another, and a plain MEMBER of a third. Lives only on {@link GroupMember}.
 * <p>
 * Enum order is meaningful: ordinal() is used as a privilege ranking
 * (lower ordinal = more privileged) by {@code security.GroupSecurityService}.
 */
public enum GroupRole {
    OWNER,
    ADMIN,
    MEMBER
}
