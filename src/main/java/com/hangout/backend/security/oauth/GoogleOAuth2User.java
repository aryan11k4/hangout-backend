package com.hangout.backend.security.oauth;

import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Wraps Google's OAuth2User so the userId we resolved in
 * CustomOAuth2UserService can be carried through to
 * OAuth2LoginSuccessHandler without a redundant DB lookup there.
 */
public class GoogleOAuth2User implements OAuth2User {

    private final OAuth2User delegate;
    private final UUID userId;

    public GoogleOAuth2User(OAuth2User delegate, UUID userId) {
        this.delegate = delegate;
        this.userId = userId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}