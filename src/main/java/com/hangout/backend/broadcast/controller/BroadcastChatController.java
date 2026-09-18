package com.hangout.backend.broadcast.controller;

import com.hangout.backend.broadcast.dto.BroadcastChatMessageDto;
import com.hangout.backend.broadcast.service.BroadcastChatService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BroadcastChatController {

    private final BroadcastChatService broadcastChatService;

    /** Read-only - sending happens over WS at /app/broadcast.{broadcastId}.chat, see BroadcastChatWebSocketController. */
    @GetMapping("api/broadcasts/{broadcastId}/messages")
    public ResponseEntity<Page<BroadcastChatMessageDto>> getHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId,
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(broadcastChatService.getHistory(currentUser.getId(), broadcastId, pageable));
    }
}