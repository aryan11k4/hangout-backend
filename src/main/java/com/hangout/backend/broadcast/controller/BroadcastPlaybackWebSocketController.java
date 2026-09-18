package com.hangout.backend.broadcast.controller;

import com.hangout.backend.broadcast.dto.PlaybackCommandDto;
import com.hangout.backend.broadcast.dto.PlaybackEventDto;
import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.service.BroadcastPlaybackService;
import com.hangout.backend.common.enums.PlaybackEventType;
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
 * Client destinations (broadcastId is a path variable, not in the payload -
 * the server always trusts the destination over anything the client might
 * claim in the body):
 *   /app/broadcast.{broadcastId}.play
 *   /app/broadcast.{broadcastId}.pause
 *   /app/broadcast.{broadcastId}.seek
 *   /app/broadcast.{broadcastId}.requestSync
 * <p>
 * All PLAY/PAUSE/SEEK commands are host-only - BroadcastPlaybackService
 * throws ForbiddenException if a non-host sends one; Spring's default STOMP
 * error handling routes that back to the sender only, it is never
 * broadcast.
 * <p>
 * Results broadcast to /topic/broadcast.{broadcastId} - every subscriber
 * (synced viewers) receives it. Viewers who clicked "Break Sync" are
 * expected to still receive this on the wire but simply ignore it
 * client-side - see workflow point H, this is deliberately NOT server-side
 * state.
 */
@Controller
@RequiredArgsConstructor
public class BroadcastPlaybackWebSocketController {

    private final BroadcastPlaybackService broadcastPlaybackService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String BROADCAST_TOPIC_PREFIX = "/topic/broadcast.";

    @MessageMapping("/broadcast.{broadcastId}.play")
    public void play(@DestinationVariable UUID broadcastId, @Payload PlaybackCommandDto command, Principal principal) {
        handleEvent(broadcastId, command, principal, PlaybackEventType.PLAY, true);
    }

    @MessageMapping("/broadcast.{broadcastId}.pause")
    public void pause(@DestinationVariable UUID broadcastId, @Payload PlaybackCommandDto command, Principal principal) {
        handleEvent(broadcastId, command, principal, PlaybackEventType.PAUSE, false);
    }

    @MessageMapping("/broadcast.{broadcastId}.seek")
    public void seek(@DestinationVariable UUID broadcastId, @Payload PlaybackCommandDto command, Principal principal) {
        UserPrincipal user = (UserPrincipal) principal;
        Broadcast updated = broadcastPlaybackService.applySeek(user.getId(), broadcastId, command.position());

        messagingTemplate.convertAndSend(
                BROADCAST_TOPIC_PREFIX + broadcastId,
                new PlaybackEventDto(PlaybackEventType.SEEK, updated.getCurrentPosition(), System.currentTimeMillis())
        );
    }

    @MessageMapping("/broadcast.{broadcastId}.requestSync")
    public void requestSync(@DestinationVariable UUID broadcastId, Principal principal) {
        UserPrincipal user = (UserPrincipal) principal;
        Broadcast broadcast = broadcastPlaybackService.getForSync(user.getId(), broadcastId);

        // sent only to the requester's private queue, not broadcast to everyone
        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/broadcast-sync",
                new PlaybackEventDto(PlaybackEventType.SYNC_STATE,
                        broadcastPlaybackService.computeLivePosition(broadcast), System.currentTimeMillis())
        );
    }

    private void handleEvent(UUID broadcastId, PlaybackCommandDto command, Principal principal,
                             PlaybackEventType type, boolean isPlaying) {
        UserPrincipal user = (UserPrincipal) principal;
        Broadcast updated = broadcastPlaybackService.applyPlayPause(user.getId(), broadcastId, command.position(), isPlaying);

        messagingTemplate.convertAndSend(
                BROADCAST_TOPIC_PREFIX + broadcastId,
                new PlaybackEventDto(type, updated.getCurrentPosition(), System.currentTimeMillis())
        );
    }
}