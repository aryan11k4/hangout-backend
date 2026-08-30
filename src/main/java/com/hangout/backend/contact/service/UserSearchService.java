package com.hangout.backend.contact.service;

import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.contact.dto.ContactUserDto;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserSearchService {

    private final UserRepository userRepository;

    public ContactUserDto searchByUsernameAndCode(String username, String code) {
        User user = userRepository.findByUsernameAndContactCode(username, code)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with that username and code"));

        return new ContactUserDto(user.getId(), user.getUsername(), user.getContactCode());
    }
}