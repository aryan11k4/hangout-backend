package com.hangout.backend.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Returns a plain JSON 401 instead of Spring Security's default redirect-to-
 * login behaviour, since this is a stateless JSON API. Body is built by hand
 * (no ObjectMapper) to avoid depending on Jackson's exact package coordinates,
 * which are in flux for the Jackson-3 line that ships with Spring Boot 4.
 */
@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\","
                + "\"message\":\"Authentication is required to access this resource\",\"path\":\"%s\"}"
                .formatted(Instant.now(), escape(request.getRequestURI()));

        response.getWriter().write(json);
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
