package com.hangout.backend.group.controller;

import com.hangout.backend.group.dto.GroupCreateDto;
import com.hangout.backend.group.dto.GroupResponseDto;
import com.hangout.backend.group.service.GroupService;
import com.hangout.backend.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponseDto> createGroup(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody GroupCreateDto request) {
        GroupResponseDto response = groupService.createGroup(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponseDto> getGroup(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupService.getGroup(groupId));
    }

    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<GroupResponseDto> joinByInviteCode(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String inviteCode) {
        return ResponseEntity.ok(groupService.joinByInviteCode(currentUser.getId(), inviteCode));
    }
}