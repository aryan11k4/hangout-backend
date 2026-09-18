package com.hangout.backend.broadcast.service;

import com.hangout.backend.broadcast.dto.BroadcastChatMessageDto;
import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.entity.BroadcastMessage;
import com.hangout.backend.broadcast.repository.BroadcastMessageRepository;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.common.exception.ForbiddenException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BroadcastChatService {

    private final BroadcastMessageRepository broadcastMessageRepository;
    private final BroadcastRepository broadcastRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public BroadcastChatMessageDto sendMessage(UUID senderId, UUID broadcastId, String content) {
        Broadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found"));
        requireGroupMember(broadcast.getGroupId(), senderId);

        BroadcastMessage message = BroadcastMessage.builder()
                .broadcastId(broadcastId)
                .senderId(senderId)
                .content(content)
                .build();
        BroadcastMessage saved = broadcastMessageRepository.saveAndFlush(message);
        return toDto(saved);
    }

    public Page<BroadcastChatMessageDto> getHistory(UUID requesterId, UUID broadcastId, Pageable pageable) {
        Broadcast broadcast = broadcastRepository.findById(broadcastId)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found"));
        requireGroupMember(broadcast.getGroupId(), requesterId);

        return broadcastMessageRepository.findByBroadcastIdOrderByCreatedAtDesc(broadcastId, pageable)
                .map(this::toDto);
    }

    private void requireGroupMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ForbiddenException("You are not a member of this group");
        }
    }

    private BroadcastChatMessageDto toDto(BroadcastMessage message) {
        User sender = userRepository.findById(message.getSenderId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));
        return new BroadcastChatMessageDto(
                message.getId(),
                message.getBroadcastId(),
                message.getSenderId(),
                sender.getUsername(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}