package com.hangout.backend.message.controller;

import com.hangout.backend.message.dto.PrivateMessageRequestDto;
import com.hangout.backend.message.dto.PrivateMessageResponseDto;
import com.hangout.backend.message.service.PrivateMessageService;
import com.hangout.backend.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/private-messages")
public class PrivateMessageController {

    private final PrivateMessageService messageService;

    @PostMapping
    public ResponseEntity<PrivateMessageResponseDto> sendMessage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody PrivateMessageRequestDto request) {
        PrivateMessageResponseDto response = messageService.sendMessage(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{otherUserId}")
    public ResponseEntity<Page<PrivateMessageResponseDto>> getConversation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID otherUserId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<PrivateMessageResponseDto> conversation =
                messageService.getConversation(currentUser.getId(), otherUserId, pageable);
        return ResponseEntity.ok(conversation);
    }
}