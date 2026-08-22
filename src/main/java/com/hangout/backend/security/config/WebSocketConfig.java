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
 * Endpoint: /ws (SockJS fallback included for browsers/networks that block
 * raw WebSocket upgrades).
 * <p>
 * Client sends to:      /app/private-message.send
 * Client subscribes to: /user/queue/private-messages   (per-user private queue)
 * <p>
 * The "/user" prefix + convertAndSendToUser(...) in the service/controller
 * is how Spring routes a message to one specific authenticated user's
 * session, which is exactly what one-to-one messaging needs.
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
                .setHandshakeHandler(new StompPrincipalHandshakeHandler())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app"); // client -> server
        registry.enableSimpleBroker("/user", "/topic");     // server -> client
        registry.setUserDestinationPrefix("/user");         // enables convertAndSendToUser
    }
}