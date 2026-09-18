package com.hangout.backend.broadcast.service;

import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.common.enums.BroadcastStatus;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.List;

/**
 * Per the product decision "if the host leaves, the broadcast ends" - this
 * covers the ABRUPT case (closed tab, lost connection, crashed app), not
 * just the explicit POST /broadcasts/{id}/end call. Without this, a host
 * who disconnects without clicking "End Broadcast" leaves a permanently
 * ACTIVE broadcast that nobody else has permission to end (playback
 * commands and /end are both host-only).
 */
@Component
@RequiredArgsConstructor
public class BroadcastDisconnectListener {

    private final BroadcastRepository broadcastRepository;
    private final BroadcastService broadcastService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = accessor.getUser();

        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return;
        }

        List<Broadcast> hostedBroadcasts =
                broadcastRepository.findByHostIdAndStatus(userPrincipal.getId(), BroadcastStatus.ACTIVE);

        for (Broadcast broadcast : hostedBroadcasts) {
            broadcastService.endBroadcastInternal(broadcast, "HOST_DISCONNECTED");
        }
    }
}