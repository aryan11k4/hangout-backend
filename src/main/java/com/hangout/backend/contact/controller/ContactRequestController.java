package com.hangout.backend.contact.controller;

import com.hangout.backend.contact.dto.ContactRequestCreateDto;
import com.hangout.backend.contact.dto.ContactRequestResponseDto;
import com.hangout.backend.contact.service.ContactRequestService;
import com.hangout.backend.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/contact-requests")
public class ContactRequestController {

    private final ContactRequestService contactRequestService;

    @PostMapping
    public ResponseEntity<ContactRequestResponseDto> sendRequest(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ContactRequestCreateDto request) {
        ContactRequestResponseDto response =
                contactRequestService.sendRequest(currentUser.getId(), request.receiverId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/received")
    public ResponseEntity<List<ContactRequestResponseDto>> getReceivedRequests(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(contactRequestService.getReceivedRequests(currentUser.getId()));
    }

    @PostMapping("/{requestId}/accept")
    public ResponseEntity<Void> accept(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID requestId) {
        contactRequestService.acceptRequest(currentUser.getId(), requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID requestId) {
        contactRequestService.rejectRequest(currentUser.getId(), requestId);
        return ResponseEntity.noContent().build();
    }
}