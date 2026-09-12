package com.hangout.backend.message.controller;

import com.hangout.backend.message.dto.GroupMessageRequestDto;
import com.hangout.backend.message.dto.GroupMessageResponseDto;
import com.hangout.backend.message.service.GroupMessageService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * WebSocket (STOMP) entry point for sending a group message.
 * <p>
 * Client sends to /app/group-message.send with a GroupMessageRequestDto.
 * Membership is re-checked here (not just at subscribe time) so a member
 * who gets kicked mid-session can't keep sending - see
 * GroupMessageService.requireMember.
 * <p>
 * Delivery is a broadcast to /topic/group.{groupId} - anyone subscribed
 * receives it, unlike private messages which target one specific user via
 * convertAndSendToUser. Subscribe-time membership is enforced separately by
 * GroupChannelInterceptor, since Spring doesn't check that automatically.
 */
@Controller
@RequiredArgsConstructor
public class GroupMessageWebSocketController {

    private final GroupMessageService groupMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/group-message.send")
    public void sendMessage(@Payload GroupMessageRequestDto request, Principal principal) {
        UserPrincipal currentUser = (UserPrincipal) principal;

        GroupMessageResponseDto saved = groupMessageService.sendMessage(currentUser.getId(), request);

        messagingTemplate.convertAndSend("/topic/group." + request.groupId(), saved);
    }
}