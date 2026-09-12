package com.hangout.backend.group.service;

import com.hangout.backend.common.enums.GroupRole;
import com.hangout.backend.common.enums.GroupVisibility;
import com.hangout.backend.common.enums.JoinRequestStatus;
import com.hangout.backend.common.exception.DuplicateResourceException;
import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.common.exception.InvalidRequestException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.dto.JoinRequestResponseDto;
import com.hangout.backend.group.entity.Group;
import com.hangout.backend.group.entity.GroupMember;
import com.hangout.backend.group.entity.JoinRequest;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.group.repository.GroupRepository;
import com.hangout.backend.group.repository.JoinRequestRepository;
import com.hangout.backend.notification.dto.NotificationType;
import com.hangout.backend.notification.service.NotificationService;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JoinRequestService {

    private final JoinRequestRepository joinRequestRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public JoinRequestResponseDto requestToJoin(UUID userId, UUID groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        if (group.getVisibility() != GroupVisibility.PUBLIC) {
            throw new ForbiddenException("This group is private - a join request requires the invite code");
        }

        return createPendingRequest(userId, group);
    }

    @Transactional
    public JoinRequestResponseDto requestToJoinByCode(UUID userId, String inviteCode) {
        Group group = groupRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid invite code"));

        return createPendingRequest(userId, group);
    }

    private JoinRequestResponseDto createPendingRequest(UUID userId, Group group) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new DuplicateResourceException("You are already a member of this group");
        }

        joinRequestRepository.findByGroupIdAndUserId(group.getId(), userId).ifPresent(existing -> {
            if (existing.getStatus() == JoinRequestStatus.PENDING) {
                throw new DuplicateResourceException("A join request is already pending");
            }
        });

        JoinRequest request = JoinRequest.builder()
                .group(group)
                .user(user)
                .status(JoinRequestStatus.PENDING)
                .build();
        JoinRequest saved = joinRequestRepository.saveAndFlush(request);

        notifyApprovers(group, user);

        return toDto(saved);
    }

    public List<JoinRequestResponseDto> getPendingRequests(UUID actorId, UUID groupId) {
        requireApprover(actorId, groupId);
        return joinRequestRepository.findByGroupIdAndStatus(groupId, JoinRequestStatus.PENDING).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void approveRequest(UUID actorId, UUID groupId, UUID requestId) {
        requireApprover(actorId, groupId);
        JoinRequest request = getPendingRequestForGroup(groupId, requestId);

        request.setStatus(JoinRequestStatus.APPROVED);
        joinRequestRepository.save(request);

        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, request.getUser().getId())) {
            groupMemberRepository.save(GroupMember.builder()
                    .group(request.getGroup())
                    .user(request.getUser())
                    .role(GroupRole.MEMBER)
                    .build());
        }

        User actor = userRepository.findById(actorId).orElseThrow();
        notificationService.notify(
                request.getUser().getId(),
                NotificationType.JOIN_REQUEST_APPROVED,
                "Your request to join \"" + request.getGroup().getName() + "\" was approved",
                request.getGroup().getId(),
                request.getGroup().getName(),
                actorId,
                actor.getUsername()
        );
    }

    @Transactional
    public void rejectRequest(UUID actorId, UUID groupId, UUID requestId) {
        requireApprover(actorId, groupId);
        JoinRequest request = getPendingRequestForGroup(groupId, requestId);
        request.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(request);

        User actor = userRepository.findById(actorId).orElseThrow();
        notificationService.notify(
                request.getUser().getId(),
                NotificationType.JOIN_REQUEST_REJECTED,
                "Your request to join \"" + request.getGroup().getName() + "\" was rejected",
                request.getGroup().getId(),
                request.getGroup().getName(),
                actorId,
                actor.getUsername()
        );
    }

    /** Notifies every OWNER/ADMIN of the group that a new join request came in. */
    private void notifyApprovers(Group group, User requester) {
        List<GroupMember> approvers = groupMemberRepository.findByGroupId(group.getId()).stream()
                .filter(m -> m.getRole() == GroupRole.OWNER || m.getRole() == GroupRole.ADMIN)
                .toList();

        for (GroupMember approver : approvers) {
            notificationService.notify(
                    approver.getUser().getId(),
                    NotificationType.JOIN_REQUEST_RECEIVED,
                    requester.getUsername() + " requested to join \"" + group.getName() + "\"",
                    group.getId(),
                    group.getName(),
                    requester.getId(),
                    requester.getUsername()
            );
        }
    }

    private JoinRequest getPendingRequestForGroup(UUID groupId, UUID requestId) {
        JoinRequest request = joinRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Join request not found"));
        if (!request.getGroup().getId().equals(groupId)) {
            throw new InvalidRequestException("Request does not belong to this group");
        }
        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new InvalidRequestException("This request has already been " + request.getStatus());
        }
        return request;
    }

    private void requireApprover(UUID actorId, UUID groupId) {
        GroupMember actor = groupMemberRepository.findByGroupIdAndUserId(groupId, actorId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this group"));
        if (actor.getRole() != GroupRole.OWNER && actor.getRole() != GroupRole.ADMIN) {
            throw new ForbiddenException("Only the owner or an admin can manage join requests");
        }
    }

    private JoinRequestResponseDto toDto(JoinRequest request) {
        return new JoinRequestResponseDto(
                request.getId(),
                request.getGroup().getId(),
                request.getUser().getId(),
                request.getUser().getUsername(),
                request.getStatus(),
                request.getRequestedAt()
        );
    }
}