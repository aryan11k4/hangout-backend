package com.hangout.backend.contact.service;

import com.hangout.backend.common.exception.DuplicateResourceException;
import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ContactCodeService {

    private final UserRepository userRepository;

    @Transactional
    public void setContactCode(UUID userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // scoped uniqueness: (username, code) pair, not code alone
        boolean taken = userRepository.existsByUsernameAndContactCodeAndIdNot(
                user.getUsername(), code, userId);
        if (taken) {
            throw new DuplicateResourceException("Code " + code + " is already used by this username");
        }

        user.setContactCode(code);
        userRepository.save(user);
    }
}