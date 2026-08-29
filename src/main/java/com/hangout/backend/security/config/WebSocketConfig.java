package com.hangout.backend.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP-over-WebSocket setup.
 * <p>
 * Endpoint: /ws (plain WebSocket, no SockJS).
 * <p>
 * Client sends to:      /app/private-message.send
 * Client subscribes to: /user/queue/private-messages   (per-user private queue)
 * <p>
 * IMPORTANT: "/user" must NOT be listed in enableSimpleBroker(...). It is a
 * reserved prefix handled separately by Spring's UserDestinationMessageHandler,
 * which rewrites "/user/queue/private-messages" into a session-specific
 * physical destination like "/queue/private-messages-userABC123" before the
 * broker ever sees it. Registering "/user" as an actual broker destination
 * prefix causes it to be treated as a literal topic instead of being
 * rewritten, so convertAndSendToUser(...) silently goes nowhere.
 * setUserDestinationPrefix("/user") below is what actually wires up the
 * per-user routing - the broker itself only needs to know about "/topic"
 * and "/queue".
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

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
}