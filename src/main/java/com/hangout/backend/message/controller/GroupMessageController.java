package com.hangout.backend.message.controller;

import com.hangout.backend.message.dto.GroupMessageResponseDto;
import com.hangout.backend.message.service.GroupMessageService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/groups/{groupId}/messages")
public class GroupMessageController {

    private final GroupMessageService groupMessageService;

    @GetMapping
    public ResponseEntity<Page<GroupMessageResponseDto>> getHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(groupMessageService.getHistory(currentUser.getId(), groupId, pageable));
    }
}