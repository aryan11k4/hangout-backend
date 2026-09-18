package com.hangout.backend.broadcast.controller;

import com.hangout.backend.broadcast.dto.BroadcastChatMessageDto;
import com.hangout.backend.broadcast.dto.BroadcastChatSendDto;
import com.hangout.backend.broadcast.service.BroadcastChatService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

/**
 * Client sends to /app/broadcast.{broadcastId}.chat, server persists and
 * broadcasts to /topic/broadcast.{broadcastId} alongside playback events -
 * same topic carries both, differentiated by the "type" field in the
 * payload (CHAT_MESSAGE vs PLAY/PAUSE/SEEK/etc).
 */
@Controller
@RequiredArgsConstructor
public class BroadcastChatWebSocketController {

    private final BroadcastChatService broadcastChatService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String BROADCAST_TOPIC_PREFIX = "/topic/broadcast.";

    @MessageMapping("/broadcast.{broadcastId}.chat")
    public void sendMessage(@DestinationVariable UUID broadcastId,
                            @Payload BroadcastChatSendDto request,
                            Principal principal) {
        UserPrincipal user = (UserPrincipal) principal;

        BroadcastChatMessageDto saved =
                broadcastChatService.sendMessage(user.getId(), broadcastId, request.content());

        messagingTemplate.convertAndSend(BROADCAST_TOPIC_PREFIX + broadcastId, saved);
    }
}