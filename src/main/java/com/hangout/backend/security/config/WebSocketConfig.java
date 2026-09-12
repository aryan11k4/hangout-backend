package com.hangout.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket setup.
 * <p>
 * Endpoint: /ws (plain WebSocket, no SockJS).
 * <p>
 * Client sends to:
 *   /app/private-message.send
 *   /app/group-message.send
 * Client subscribes to:
 *   /user/queue/private-messages          (per-user private queue)
 *   /topic/group.{groupId}                (broadcast, membership-gated - see GroupChannelInterceptor)
 * <p>
 * IMPORTANT: "/user" must NOT be listed in enableSimpleBroker(...). It is a
 * reserved prefix handled separately by Spring's UserDestinationMessageHandler,
 * which rewrites "/user/queue/private-messages" into a session-specific
 * physical destination before the broker ever sees it. Registering "/user"
 * as a literal broker prefix breaks that rewrite silently (see private
 * messaging notes) - the broker only needs to own "/topic" and "/queue".
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;
    private final GroupChannelInterceptor groupChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // tighten before prod, same as CORS
                .addInterceptors(jwtHandshakeInterceptor)
                .setHandshakeHandler(new StompPrincipalHandshakeHandler());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // client -> server
        registry.enableSimpleBroker("/topic", "/queue");    // server -> client (NOT "/user")
        registry.setUserDestinationPrefix("/user");         // enables convertAndSendToUser
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // runs on every inbound STOMP frame (CONNECT, SUBSCRIBE, SEND, ...),
        // unlike the handshake interceptor which only runs once at connect time
        registration.interceptors(groupChannelInterceptor);
    }
}