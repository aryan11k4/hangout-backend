package com.hangout.backend.group.controller;

import com.hangout.backend.group.dto.JoinRequestByCodeDto;
import com.hangout.backend.group.dto.JoinRequestResponseDto;
import com.hangout.backend.group.service.JoinRequestService;
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
public class JoinRequestController {

    private final JoinRequestService joinRequestService;

    /** PUBLIC groups only - group must be visibility=PUBLIC or this is rejected. */
    @PostMapping("api/groups/{groupId}/join-requests")
    public ResponseEntity<JoinRequestResponseDto> requestToJoin(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId) {
        JoinRequestResponseDto response = joinRequestService.requestToJoin(currentUser.getId(), groupId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PRIVATE groups - caller proves they have the invite code. Still
     * creates a PENDING request needing approval, unlike
     * POST /api/groups/join/{inviteCode} which joins instantly.
     * No {groupId} in the path here on purpose - the caller doesn't
     * necessarily know the group's id, only its invite code.
     */
    @PostMapping("api/groups/join-requests/by-code")
    public ResponseEntity<JoinRequestResponseDto> requestToJoinByCode(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody JoinRequestByCodeDto request) {
        JoinRequestResponseDto response =
                joinRequestService.requestToJoinByCode(currentUser.getId(), request.inviteCode());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("api/groups/{groupId}/join-requests")
    public ResponseEntity<List<JoinRequestResponseDto>> getPendingRequests(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(joinRequestService.getPendingRequests(currentUser.getId(), groupId));
    }

    @PostMapping("api/groups/{groupId}/join-requests/{requestId}/approve")
    public ResponseEntity<Void> approve(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @PathVariable UUID requestId) {
        joinRequestService.approveRequest(currentUser.getId(), groupId, requestId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("api/groups/{groupId}/join-requests/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @PathVariable UUID requestId) {
        joinRequestService.rejectRequest(currentUser.getId(), groupId, requestId);
        return ResponseEntity.noContent().build();
    }
}