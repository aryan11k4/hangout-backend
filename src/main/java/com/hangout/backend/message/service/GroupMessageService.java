package com.hangout.backend.message.service;

import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.message.dto.GroupMessageRequestDto;
import com.hangout.backend.message.dto.GroupMessageResponseDto;
import com.hangout.backend.message.entity.GroupMessage;
import com.hangout.backend.message.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupMessageService {

    private final GroupMessageRepository groupMessageRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional
    public GroupMessageResponseDto sendMessage(UUID senderId, GroupMessageRequestDto request) {
        requireMember(request.groupId(), senderId);

        GroupMessage message = GroupMessage.builder()
                .groupId(request.groupId())
                .senderId(senderId)
                .content(request.content())
                .build();

        GroupMessage saved = groupMessageRepository.saveAndFlush(message);
        return toDto(saved);
    }

    public Page<GroupMessageResponseDto> getHistory(UUID requesterId, UUID groupId, Pageable pageable) {
        requireMember(groupId, requesterId);
        return groupMessageRepository.findByGroupIdOrderByCreatedAtDesc(groupId, pageable)
                .map(this::toDto);
    }

    /** Also used by the WS layer to verify membership at SUBSCRIBE time - see GroupChannelInterceptor. */
    public void requireMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    private GroupMessageResponseDto toDto(GroupMessage message) {
        return new GroupMessageResponseDto(
                message.getId(),
                message.getGroupId(),
                message.getSenderId(),
                message.isDeleted() ? "[deleted]" : message.getContent(),
                message.getMessageType(),
                message.getCreatedAt(),
                message.isEdited()
        );
    }
}