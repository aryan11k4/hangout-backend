package com.hangout.backend.auth.controller;

import com.hangout.backend.auth.dto.SetPasswordDto;
import com.hangout.backend.auth.service.PasswordSetupService;
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
@RequestMapping("api/users/me/password")
public class PasswordSetupController {

    private final PasswordSetupService passwordSetupService;

    @PutMapping
    public ResponseEntity<Void> setPassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody SetPasswordDto request) {
        passwordSetupService.setPassword(currentUser.getId(), request.newPassword());
        return ResponseEntity.noContent().build();
    }
}