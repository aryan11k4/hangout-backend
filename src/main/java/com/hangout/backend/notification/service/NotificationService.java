package com.hangout.backend.notification.service;

import com.hangout.backend.notification.dto.NotificationDto;
import com.hangout.backend.notification.dto.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Pushes ephemeral notifications to a specific user's WS session, reusing
 * the same convertAndSendToUser + /user destination-prefix mechanism as
 * private messages. Nothing here is persisted - if the user isn't
 * connected, the notification is simply lost. See WebSocketConfig for why
 * "/user" must stay out of enableSimpleBroker for this to actually deliver.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final String NOTIFICATION_QUEUE = "/queue/notifications";

    private final SimpMessagingTemplate messagingTemplate;

    public void notify(UUID recipientId, NotificationType type, String message,
                       UUID groupId, String groupName, UUID actorId, String actorUsername) {
        NotificationDto payload = new NotificationDto(
                type, message, groupId, groupName, actorId, actorUsername, Instant.now()
        );
        messagingTemplate.convertAndSendToUser(recipientId.toString(), NOTIFICATION_QUEUE, payload);
    }
}