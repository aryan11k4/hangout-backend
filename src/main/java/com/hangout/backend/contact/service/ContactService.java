package com.hangout.backend.contact.service;

import com.hangout.backend.common.exception.ResourceNotFoundException;
import com.hangout.backend.contact.dto.ContactUserDto;
import com.hangout.backend.contact.repository.ContactRepository;
import com.hangout.backend.user.entity.User;
import com.hangout.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContactService {

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public List<ContactUserDto> getContacts(UUID userId) {
        return contactRepository.findByUserId(userId).stream()
                .map(contact -> {
                    User user = userRepository.findById(contact.getContactUserId())
                            .orElseThrow(() -> new ResourceNotFoundException("Contact user not found"));
                    return new ContactUserDto(user.getId(), user.getUsername(), user.getContactCode());
                })
                .toList();
    }
}