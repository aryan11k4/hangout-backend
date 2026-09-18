package com.hangout.backend.security.oauth;

import com.hangout.backend.common.enums.Role;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Runs after Google confirms the user's identity but before Spring builds
 * the final Authentication object. Maps "a Google account with this
 * googleId/email" onto a row in our users table - creating one if it
 * doesn't exist, or silently linking googleId onto an existing
 * password-based account if the email already matches one (auto-link on
 * verified email match, since Google guarantees the email is verified).
 * <p>
 * New Google-only signups get password = null. This is safe now that
 * User.password is nullable - AuthService's password-login path must
 * check for null and reject with a clear error ("this account uses Google
 * Sign-In") rather than calling passwordEncoder.matches(raw, null), which
 * would NPE. See AuthService-OAuth-CHANGES.txt.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");
        String name = oAuth2User.getAttribute("name");

        if (googleId == null || email == null) {
            throw new OAuth2AuthenticationException("Google did not return a usable id/email");
        }
        if (emailVerified == null || !emailVerified) {
            throw new OAuth2AuthenticationException("Google email is not verified");
        }

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existing -> linkGoogleAccount(existing, googleId))
                        .orElseGet(() -> createNewGoogleUser(googleId, email, name)));

        return new GoogleOAuth2User(oAuth2User, user.getId());
    }

    private User linkGoogleAccount(User existing, String googleId) {
        existing.setGoogleId(googleId);
        return userRepository.save(existing);
    }

    private User createNewGoogleUser(String googleId, String email, String name) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .googleId(googleId)
                .email(email)
                .username(deriveUsername(email))
                .displayName(name)
                .password(null) // no password - login via Google only, unless they set one later
                .authProvider("GOOGLE")
                .role(Role.USER)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }

    private String deriveUsername(String email) {
        String base = email.substring(0, email.indexOf('@'));
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }
}