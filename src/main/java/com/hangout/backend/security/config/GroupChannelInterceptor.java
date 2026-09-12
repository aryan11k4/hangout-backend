package com.hangout.backend.security.config;

import com.hangout.backend.security.principal.UserPrincipal;
import com.hangout.backend.group.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;

/**
 * Blocks a STOMP SUBSCRIBE to /topic/group.{groupId} unless the subscribing
 * user is actually a member of that group. Nothing else in the stack checks
 * this - SecurityConfig's HTTP filter chain only covers the initial
 * handshake, not individual STOMP frames sent afterward. Without this, any
 * authenticated user (member of ANY group or none) could subscribe to any
 * group's topic and silently read its messages, since /topic destinations
 * are otherwise open pub/sub with no per-destination access control.
 * <p>
 * SEND-time membership is checked separately, in GroupMessageService -
 * this interceptor only guards read access via SUBSCRIBE.
 */
@Component
@RequiredArgsConstructor
public class GroupChannelInterceptor implements ChannelInterceptor {

    private static final String GROUP_TOPIC_PREFIX = "/topic/group.";

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();

            if (destination != null && destination.startsWith(GROUP_TOPIC_PREFIX)) {
                String groupIdStr = destination.substring(GROUP_TOPIC_PREFIX.length());
                UUID groupId;
                try {
                    groupId = UUID.fromString(groupIdStr);
                } catch (IllegalArgumentException e) {
                    return null; // malformed group id in destination -> block silently
                }

                Principal principal = accessor.getUser();
                if (!(principal instanceof UserPrincipal userPrincipal)) {
                    return null; // no authenticated principal -> block
                }

                boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userPrincipal.getId());
                if (!isMember) {
                    return null; // not a member -> silently drop the SUBSCRIBE frame
                }
            }
        }

        return message;
    }
}