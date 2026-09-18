package com.hangout.backend.security.oauth;

import com.hangout.backend.security.jwt.JwtService;
import com.hangout.backend.security.principal.UserPrincipal;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Runs once Google login + CustomOAuth2UserService have both succeeded.
 * Issues the SAME JWT shape as your existing password login, then
 * redirects to {app.oauth.redirect-uri}?token=<jwt>.
 * <p>
 * Uses explicit constructor injection (not @RequiredArgsConstructor) so the
 * @Value-resolved redirectUri is part of the same constructor Spring uses
 * to build this bean - avoids any ordering/mixing issues between
 * Lombok-generated constructors and field-level @Value injection.
 * <p>
 * REQUIRES app.oauth.redirect-uri to be set in application.properties:
 *   app.oauth.redirect-uri=${OAUTH_FRONTEND_REDIRECT:http://localhost:3000/oauth/callback}
 * If that property is missing entirely, this bean fails to construct at
 * startup with an UnsatisfiedDependencyException - check application.properties
 * first if this class ever fails to load again.
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final String redirectUri;

    public OAuth2LoginSuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            @Value("${app.oauth.redirect-uri}") String redirectUri) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.redirectUri = redirectUri;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        GoogleOAuth2User oauthUser = (GoogleOAuth2User) authentication.getPrincipal();

        User user = userRepository.findById(oauthUser.getUserId())
                .orElseThrow(() -> new IllegalStateException("OAuth user vanished after creation"));

        UserPrincipal principal = UserPrincipal.from(user);
        String token = jwtService.generateToken(principal);

        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUri + "?token=" + encodedToken);
    }
}