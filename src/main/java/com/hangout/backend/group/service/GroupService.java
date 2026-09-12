package com.hangout.backend.group.service;

import com.hangout.backend.common.enums.GroupRole;
import com.hangout.backend.common.exception.DuplicateResourceException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.dto.GroupCreateDto;
import com.hangout.backend.group.dto.GroupResponseDto;
import com.hangout.backend.group.entity.Group;
import com.hangout.backend.group.entity.GroupMember;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.group.repository.GroupRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupService {

    private static final int MAX_INVITE_CODE_ATTEMPTS = 5;

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public GroupResponseDto createGroup(UUID ownerId, GroupCreateDto request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Group group = Group.builder()
                .name(request.name())
                .description(request.description())
                .visibility(request.visibility())
                .owner(owner)
                .inviteCode(generateUniqueInviteCode())
                .build();
        Group saved = groupRepository.saveAndFlush(group);

        // creator is automatically OWNER
        groupMemberRepository.save(GroupMember.builder()
                .group(saved)
                .user(owner)
                .role(GroupRole.OWNER)
                .build());

        return toDto(saved);
    }

    public GroupResponseDto getGroup(UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));
        return toDto(group);
    }

    /**
     * Instant join via invite code - no JoinRequest involved. Idempotent: if
     * already a member, this is a no-op rather than an error, since "I have
     * the link and I'm already in" isn't really a conflict from the user's
     * perspective.
     */
    @Transactional
    public GroupResponseDto joinByInviteCode(UUID userId, String inviteCode) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite code"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            groupMemberRepository.save(GroupMember.builder()
                    .group(group)
                    .user(user)
                    .role(GroupRole.MEMBER)
                    .build());
        }

        return toDto(group);
    }

    private String generateUniqueInviteCode() {
        for (int i = 0; i < MAX_INVITE_CODE_ATTEMPTS; i++) {
            String code = InviteCodeGenerator.generate();
            if (groupRepository.findByInviteCode(code).isEmpty()) {
                return code;
            }
        }
        // astronomically unlikely with 8 chars from a 32-char alphabet, but
        // fail loudly rather than silently save a colliding/null code
        throw new DuplicateResourceException("Could not generate a unique invite code, please retry");
    }

    private GroupResponseDto toDto(Group group) {
        return new GroupResponseDto(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getVisibility(),
                group.getInviteCode(),
                group.getOwner().getId(),
                group.getOwner().getUsername(),
                group.getCreatedAt()
        );
    }
}