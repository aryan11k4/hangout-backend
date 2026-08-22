package com.hangout.backend.security.config;

import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Pulls the UserPrincipal that JwtHandshakeInterceptor placed into the
 * session attributes and wraps it as the java.security.Principal for this
 * WebSocket session - this is what @MessageMapping methods' Principal
 * parameter and convertAndSendToUser(...) rely on downstream.
 */
public class StompPrincipalHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(org.springframework.http.server.ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Object principal = attributes.get("principal");
        if (principal instanceof Principal p) {
            return p;
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}