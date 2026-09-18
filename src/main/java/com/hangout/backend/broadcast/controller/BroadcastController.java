package com.hangout.backend.broadcast.controller;

import com.hangout.backend.broadcast.dto.*;
import com.hangout.backend.broadcast.service.BroadcastService;
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
public class BroadcastController {

    private final BroadcastService broadcastService;

    @PostMapping("api/groups/{groupId}/broadcasts")
    public ResponseEntity<BroadcastResponseDto> createBroadcast(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateBroadcastDto request) {

        System.out.println("[CREATE] groupId=" + groupId + " userId=" + currentUser.getId() + " at=" + java.time.Instant.now());

        BroadcastResponseDto response = broadcastService.createBroadcast(currentUser.getId(), groupId, request);

        System.out.println("[CREATE-DONE] broadcastId=" + response.broadcastId() + " at=" + java.time.Instant.now());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("api/groups/{groupId}/broadcasts")
    public ResponseEntity<List<BroadcastSummaryDto>> getActiveBroadcasts(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId) {
        return ResponseEntity.ok(broadcastService.getActiveBroadcasts(currentUser.getId(), groupId));
    }

    /** Covers both "get broadcast info" and "get current sync state" - always includes serverTime. */
    @GetMapping("api/broadcasts/{broadcastId}")
    public ResponseEntity<BroadcastResponseDto> getBroadcast(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId) {
        return ResponseEntity.ok(broadcastService.getBroadcast(currentUser.getId(), broadcastId));
    }

    @PostMapping("api/broadcasts/{broadcastId}/join")
    public ResponseEntity<JoinBroadcastResponseDto> joinBroadcast(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId) {
        return ResponseEntity.ok(broadcastService.joinBroadcast(currentUser.getId(), broadcastId));
    }

    @PostMapping("api/broadcasts/{broadcastId}/leave")
    public ResponseEntity<Void> leaveBroadcast(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId) {

        System.out.println("[LEAVE] broadcastId=" + broadcastId + " userId=" + currentUser.getId() + " at=" + java.time.Instant.now());

        broadcastService.leaveBroadcast(currentUser.getId(), broadcastId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("api/broadcasts/{broadcastId}/end")
    public ResponseEntity<Void> endBroadcast(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId) {

        System.out.println("[END] broadcastId=" + broadcastId + " userId=" + currentUser.getId() + " at=" + java.time.Instant.now());

        broadcastService.endBroadcast(currentUser.getId(), broadcastId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("api/broadcasts/{broadcastId}/viewers")
    public ResponseEntity<List<BroadcastParticipantDto>> getViewers(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID broadcastId) {
        return ResponseEntity.ok(broadcastService.getViewers(currentUser.getId(), broadcastId));
    }
}