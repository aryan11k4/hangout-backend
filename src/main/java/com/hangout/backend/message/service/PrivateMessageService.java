package com.hangout.backend.message.service;

import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.message.dto.PrivateMessageRequestDto;
import com.hangout.backend.message.dto.PrivateMessageResponseDto;
import com.hangout.backend.message.entity.PrivateMessage;
import com.hangout.backend.message.repository.PrivateMessageRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateMessageService {

    private final PrivateMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public PrivateMessageResponseDto sendMessage(UUID senderId, PrivateMessageRequestDto request) {
        if (senderId.equals(request.getReceiverId())) {
            throw new IllegalArgumentException("Cannot send a message to yourself");
        }

        userRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver not found"));

        PrivateMessage message = PrivateMessage.builder()
                .senderId(senderId)
                .receiverId(receiver.getId())
                .content(request.getContent())
                .build();

        // saveAndFlush (not save) - forces the INSERT to run immediately, so
        // @CreationTimestamp's createdAt is actually populated on the
        // returned entity before we map it to a DTO. Plain save() can defer
        // the physical INSERT until later in the transaction, which is why
        // createdAt was coming back null in the WebSocket push payload even
        // though the DB row itself ended up correct after commit.
        PrivateMessage saved = messageRepository.saveAndFlush(message);
        return toDto(saved);
    }

    public Page<PrivateMessageResponseDto> getConversation(UUID currentUserId, UUID otherUserId, Pageable pageable) {
        userRepository.findById(otherUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return messageRepository.findConversation(currentUserId, otherUserId, pageable)
                .map(this::toDto);
    }

    private PrivateMessageResponseDto toDto(PrivateMessage message) {
        return new PrivateMessageResponseDto(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.isDeleted() ? "[deleted]" : message.getContent(),
                message.getMessageType(),
                message.getCreatedAt(),
                message.isEdited()
        );
    }
}