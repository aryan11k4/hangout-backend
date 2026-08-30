package com.hangout.backend.contact.controller;

import com.hangout.backend.contact.dto.SetContactCodeDto;
import com.hangout.backend.contact.service.ContactCodeService;
import com.hangout.backend.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users/me/contact-code")
public class ContactCodeController {

    private final ContactCodeService contactCodeService;

    @PutMapping
    public ResponseEntity<Void> setContactCode(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SetContactCodeDto request) {
        contactCodeService.setContactCode(currentUser.getId(), request.code());
        return ResponseEntity.noContent().build();
    }
}