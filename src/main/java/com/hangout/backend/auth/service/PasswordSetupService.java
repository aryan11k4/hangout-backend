package com.hangout.backend.auth.service;

import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Lets a Google-only account (password == null) set a password for the
 * first time, giving them a second way to log in - or lets any user change
 * their existing password. Deliberately does NOT require the old password
 * when the account currently has none (nothing to verify against); when a
 * password already exists, consider adding an oldPassword check here for
 * extra verification - not enforced currently, flag if you want it added.
 */
@Service
@RequiredArgsConstructor
public class PasswordSetupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void setPassword(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}