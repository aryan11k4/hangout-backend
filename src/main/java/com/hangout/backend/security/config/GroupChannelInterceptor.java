package com.hangout.backend.security.config;

import com.hangout.backend.broadcast.entity.Broadcast;
import com.hangout.backend.broadcast.repository.BroadcastRepository;
import com.hangout.backend.group.repository.GroupMemberRepository;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

/**
 * Blocks a STOMP SUBSCRIBE to a group-scoped or broadcast-scoped topic
 * unless the subscribing user is actually a member of the relevant group.
 * Covers three destination shapes:
 *   /topic/group.{groupId}                 - group chat, membership is direct
 *   /topic/group.{groupId}.broadcasts      - group-wide broadcast start/end
 *                                             notifications for clients on the
 *                                             group screen who haven't joined
 *                                             any specific broadcast yet
 *   /topic/broadcast.{broadcastId}         - broadcast room, membership is
 *                                             checked via the broadcast's
 *                                             owning groupId
 * <p>
 * Nothing else in the stack checks this - SecurityConfig's HTTP filter
 * chain only covers the initial handshake, not individual STOMP frames
 * sent afterward. Without this, any authenticated user could subscribe to
 * any of these topics and silently read data they shouldn't, since /topic
 * destinations are otherwise open pub/sub with no per-destination access
 * control.
 * <p>
 * IMPORTANT ordering note: the ".broadcasts" suffix check must run BEFORE
 * the plain group-chat prefix check, since
 * "/topic/group.{groupId}.broadcasts" also starts with the same
 * "/topic/group." prefix as plain group chat - checking the more specific
 * suffix first avoids misrouting it into the group-chat branch, which
 * would otherwise try to parse "{groupId}.broadcasts" as a UUID and fail.
 * <p>
 * SEND-time authorization (membership for group messages, host-only for
 * broadcast playback commands) is checked separately in the relevant
 * services - this interceptor only guards read access via SUBSCRIBE.
 */
@Component
@RequiredArgsConstructor
public class GroupChannelInterceptor implements ChannelInterceptor {

    private static final String GROUP_TOPIC_PREFIX = "/topic/group.";
    private static final String GROUP_BROADCASTS_TOPIC_SUFFIX = ".broadcasts";
    private static final String BROADCAST_TOPIC_PREFIX = "/topic/broadcast.";

    private final GroupMemberRepository groupMemberRepository;
    private final BroadcastRepository broadcastRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || !StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            return message;
        }

        String destination = accessor.getDestination();
        if (destination == null) {
            return message;
        }

        Principal principal = accessor.getUser();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return null; // no authenticated principal -> block
        }

        // MUST be checked before the plain GROUP_TOPIC_PREFIX branch - see
        // class-level note on ordering.
        if (destination.startsWith(GROUP_TOPIC_PREFIX) && destination.endsWith(GROUP_BROADCASTS_TOPIC_SUFFIX)) {
            return checkGroupBroadcastsListMembership(destination, userPrincipal, message);
        }

        if (destination.startsWith(GROUP_TOPIC_PREFIX)) {
            return checkGroupMembership(destination, GROUP_TOPIC_PREFIX, userPrincipal, message);
        }

        if (destination.startsWith(BROADCAST_TOPIC_PREFIX)) {
            return checkBroadcastMembership(destination, userPrincipal, message);
        }

        return message;
    }

    private Message<?> checkGroupBroadcastsListMembership(String destination, UserPrincipal userPrincipal,
                                                          Message<?> message) {
        String middle = destination.substring(
                GROUP_TOPIC_PREFIX.length(),
                destination.length() - GROUP_BROADCASTS_TOPIC_SUFFIX.length()
        );
        UUID groupId = parseUuid(middle);
        if (groupId == null) return null;

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userPrincipal.getId());
        return isMember ? message : null;
    }

    private Message<?> checkGroupMembership(String destination, String prefix,
                                            UserPrincipal userPrincipal, Message<?> message) {
        UUID groupId = parseUuid(destination.substring(prefix.length()));
        if (groupId == null) return null;

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, userPrincipal.getId());
        return isMember ? message : null;
    }

    private Message<?> checkBroadcastMembership(String destination, UserPrincipal userPrincipal, Message<?> message) {
        UUID broadcastId = parseUuid(destination.substring(BROADCAST_TOPIC_PREFIX.length()));
        if (broadcastId == null) return null;

        Optional<Broadcast> broadcast = broadcastRepository.findById(broadcastId);
        if (broadcast.isEmpty()) return null;

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(
                broadcast.get().getGroupId(), userPrincipal.getId());
        return isMember ? message : null;
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}