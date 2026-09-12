package com.hangout.backend.group.service;

import com.hangout.backend.common.enums.GroupVisibility;
import com.hangout.backend.group.dto.GroupResponseDto;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.group.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupDiscoveryService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    /** Browse all PUBLIC groups, or search by name substring if provided. */
    public Page<GroupResponseDto> searchPublicGroups(String nameQuery, Pageable pageable) {
        Page<com.hangout.backend.group.entity.Group> results =
                (nameQuery == null || nameQuery.isBlank())
                        ? groupRepository.findByVisibility(GroupVisibility.PUBLIC, pageable)
                        : groupRepository.findByVisibilityAndNameContainingIgnoreCase(
                        GroupVisibility.PUBLIC, nameQuery.trim(), pageable);

        return results.map(this::toDto);
    }

    /** Groups the given user is actually a member of - for "my groups" listings. */
    public java.util.List<GroupResponseDto> getMyGroups(UUID userId) {
        return groupMemberRepository.findByUserId(userId).stream()
                .map(member -> toDto(member.getGroup()))
                .toList();
    }

    private GroupResponseDto toDto(com.hangout.backend.group.entity.Group group) {
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