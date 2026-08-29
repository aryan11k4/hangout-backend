package com.hangout.backend.message.controller;

import com.hangout.backend.message.dto.PrivateMessageRequestDto;
import com.hangout.backend.message.dto.PrivateMessageResponseDto;
import com.hangout.backend.message.service.PrivateMessageService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket (STOMP) entry point for sending a private message.
 * <p>
 * Client sends a frame to /app/private-message.send with a
 * PrivateMessageRequestDto body. The current user is resolved from the
 * authenticated STOMP session Principal (set during the handshake by
 * JwtHandshakeInterceptor + StompPrincipalHandshakeHandler) - never trust
 * a senderId in the payload.
 * <p>
 * The saved message is pushed to:
 *   - the receiver, via convertAndSendToUser (their private /user/queue/private-messages)
 *   - the sender, via the same mechanism, so their other open devices/tabs
 *     also see the message land
 */
@Controller
@RequiredArgsConstructor
public class PrivateMessageWebSocketController {

    private final PrivateMessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/private-message.send")
    public void sendMessage(@Payload PrivateMessageRequestDto request, Principal principal) {
        UserPrincipal currentUser = (UserPrincipal) principal;

        PrivateMessageResponseDto saved = messageService.sendMessage(currentUser.getId(), request);

        messagingTemplate.convertAndSendToUser(
                request.getReceiverId().toString(),
                "/queue/private-messages",
                saved
        );

        messagingTemplate.convertAndSendToUser(
                currentUser.getId().toString(),
                "/queue/private-messages",
                saved
        );
    }
}