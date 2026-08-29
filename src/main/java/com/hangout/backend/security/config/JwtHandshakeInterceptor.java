package com.hangout.backend.security.config;

import com.hangout.backend.security.jwt.JwtService;
import com.hangout.backend.security.principal.UserPrincipal;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

/**
 * Validates the JWT on the WebSocket handshake (before the connection
 * upgrades) and stashes the authenticated UserPrincipal into the WebSocket
 * session attributes, so it can be read back out as the STOMP Principal.
 * <p>
 * Client must connect with the token as a query param, e.g.:
 *   ws://localhost:8080/ws?token=<jwt>
 * (SockJS/STOMP JS clients can't easily set an Authorization header on the
 * initial handshake, so query param is the standard workaround here.)
 * <p>
 * NOTE: adjust the two marked JwtService calls below to match your actual
 * method signatures - I don't have JwtService.java's contents.
 */
@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }

        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null || token.isBlank()) {
            return false; // reject handshake -> client gets connection failure
        }

        try {
            if (!jwtService.isTokenValid(token)) {
                return false;
            }

            UUID userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return false;
            }

            attributes.put("principal", UserPrincipal.from(user));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}