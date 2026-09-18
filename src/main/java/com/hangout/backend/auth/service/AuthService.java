package com.hangout.backend.auth.service;

import com.hangout.backend.auth.dto.AuthResponse;
import com.hangout.backend.auth.dto.LoginRequest;
import com.hangout.backend.auth.dto.RegisterRequest;
import com.hangout.backend.common.enums.Role;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.common.exception.DuplicateResourceException;
import com.hangout.backend.user.mapper.UserMapper;
import com.hangout.backend.user.repository.UserRepository;
import com.hangout.backend.security.jwt.JwtService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username is already taken: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email is already registered: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .displayName(request.displayName() != null ? request.displayName() : request.username())
                .role(Role.USER)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(UserPrincipal.from(saved));

        return AuthResponse.of(token, userMapper.toSummary(saved));
    }

    public AuthResponse login(LoginRequest request) {
        // Checked BEFORE authenticationManager.authenticate() runs. If we let
        // a null-password account reach AuthenticationManager, Spring
        // Security's DaoAuthenticationProvider calls
        // passwordEncoder.matches(rawPassword, null) internally, which
        // throws IllegalArgumentException - an unchecked exception that
        // GlobalExceptionHandler's generic handler would mask as a vague
        // 500, not a clean 401. Failing fast here with BadCredentialsException
        // keeps the existing @ExceptionHandler(BadCredentialsException.class)
        // -> 401 mapping working correctly for this case too.
        User existing = userRepository.findByUsername(request.usernameOrEmail())
                .or(() -> userRepository.findByEmail(request.usernameOrEmail()))
                .orElse(null);

        if (existing != null && existing.getPassword() == null) {
            throw new BadCredentialsException(
                    "This account uses Google Sign-In. Log in with Google, " +
                            "or set a password from your account settings first.");
        }

        var authToken = new UsernamePasswordAuthenticationToken(
                request.usernameOrEmail(), request.password());
        var authentication = authenticationManager.authenticate(authToken);

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + principal.getId()));

        String token = jwtService.generateToken(principal);
        return AuthResponse.of(token, userMapper.toSummary(user));
    }
}