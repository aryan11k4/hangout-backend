package com.hangout.backend.contact.controller;

import com.hangout.backend.contact.dto.ContactUserDto;
import com.hangout.backend.contact.service.ContactService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/contacts")
public class ContactController {

    private final ContactService contactService;

    @GetMapping
    public ResponseEntity<List<ContactUserDto>> getContacts(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contactService.getContacts(currentUser.getId()));
    }
}