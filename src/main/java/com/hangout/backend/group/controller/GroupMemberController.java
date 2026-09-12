package com.hangout.backend.group.controller;

import com.hangout.backend.group.dto.ChangeRoleDto;
import com.hangout.backend.group.dto.GroupMemberResponseDto;
import com.hangout.backend.group.service.GroupMemberService;
import com.hangout.backend.security.principal.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/groups/{groupId}/members")
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @GetMapping
    public ResponseEntity<List<GroupMemberResponseDto>> getMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(groupMemberService.getMembers(groupId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @Valid @RequestBody ChangeRoleDto request) {
        groupMemberService.changeRole(currentUser.getId(), groupId, userId, request.role());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId,
            @PathVariable UUID userId) {
        groupMemberService.removeMember(currentUser.getId(), groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable UUID groupId) {
        groupMemberService.leaveGroup(currentUser.getId(), groupId);
        return ResponseEntity.noContent().build();
    }
}