package com.hangout.backend.group.service;

import com.hangout.backend.common.enums.GroupRole;
import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.common.exception.InvalidRequestException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.dto.GroupMemberResponseDto;
import com.hangout.backend.group.entity.GroupMember;
import com.hangout.backend.group.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;

    public List<GroupMemberResponseDto> getMembers(UUID groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * OWNER or ADMIN can change another member's role, with two guardrails:
     * - can't touch the OWNER's own role through this endpoint (ownership
     *   transfer, if you ever add it, should be its own explicit action)
     * - only OWNER can promote someone else to ADMIN or above (an ADMIN
     *   shouldn't be able to mint peer ADMINs)
     */
    @Transactional
    public void changeRole(UUID actorId, UUID groupId, UUID targetUserId, GroupRole newRole) {
        GroupMember actor = requireMember(groupId, actorId);
        GroupMember target = requireMember(groupId, targetUserId);

        if (target.getRole() == GroupRole.OWNER) {
            throw new InvalidRequestException("Cannot change the owner's role");
        }
        if (newRole == GroupRole.OWNER) {
            throw new InvalidRequestException("Use group ownership transfer to assign OWNER, not this endpoint");
        }
        if (actor.getRole() != GroupRole.OWNER) {
            throw new ForbiddenException("Only the group owner can change member roles");
        }

        target.setRole(newRole);
        groupMemberRepository.save(target);
    }

    /**
     * OWNER or ADMIN can remove a member. An ADMIN cannot remove another
     * ADMIN or the OWNER - only OWNER can do that.
     */
    @Transactional
    public void removeMember(UUID actorId, UUID groupId, UUID targetUserId) {
        GroupMember actor = requireMember(groupId, actorId);
        GroupMember target = requireMember(groupId, targetUserId);

        if (target.getRole() == GroupRole.OWNER) {
            throw new InvalidRequestException("Cannot remove the group owner");
        }
        boolean actorIsOwner = actor.getRole() == GroupRole.OWNER;
        boolean actorIsAdmin = actor.getRole() == GroupRole.ADMIN;

        if (!actorIsOwner && !actorIsAdmin) {
            throw new ForbiddenException("Only the owner or an admin can remove members");
        }
        if (actorIsAdmin && target.getRole() == GroupRole.ADMIN) {
            throw new ForbiddenException("Only the owner can remove another admin");
        }

        groupMemberRepository.delete(target);
    }

    /** A member removing themselves. Owner cannot leave without transferring ownership first. */
    @Transactional
    public void leaveGroup(UUID userId, UUID groupId) {
        GroupMember member = requireMember(groupId, userId);
        if (member.getRole() == GroupRole.OWNER) {
            throw new InvalidRequestException("Transfer ownership before leaving the group");
        }
        groupMemberRepository.delete(member);
    }

    private GroupMember requireMember(UUID groupId, UUID userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));
    }

    private GroupMemberResponseDto toDto(GroupMember member) {
        return new GroupMemberResponseDto(
                member.getUser().getId(),
                member.getUser().getUsername(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}