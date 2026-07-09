package com.hangout.backend.security.permission;

import com.hangout.backend.common.enums.GroupRole;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Group-scoped authorization checks, exposed as a SpEL bean ("groupSecurity")
 * for use in @PreAuthorize, e.g.:
 * <pre>
 *   @PreAuthorize("@groupSecurity.hasAtLeastRole(#groupId, T(com.hangout.backend.common.enums.GroupRole).ADMIN, authentication)")
 * </pre>
 * System roles (USER/ADMIN) are checked the normal way via hasRole()/hasAuthority()
 * against UserPrincipal's authorities. Group roles (OWNER/ADMIN/MEMBER) are
 * per-group and can't live on the authorities list, so they're resolved here
 * against the group_members table instead.
 */
@Component("groupSecurity")
@RequiredArgsConstructor
public class GroupSecurityService {

    private final GroupMemberRepository groupMemberRepository;

    public boolean isMember(UUID groupId, Authentication authentication) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId(authentication));
    }

    /** True if the caller's role in this group is at least as privileged as {@code required}. */
    public boolean hasAtLeastRole(UUID groupId, GroupRole required, Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .map(member -> member.getRole().ordinal() <= required.ordinal())
                .orElse(false);
    }

    public boolean isOwner(UUID groupId, Authentication authentication) {
        return hasAtLeastRole(groupId, GroupRole.OWNER, authentication);
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal principal)) {
            throw new IllegalStateException("No authenticated UserPrincipal in security context");
        }
        return principal.getId();
    }
}
