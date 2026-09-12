package com.hangout.backend.group.controller;

import com.hangout.backend.group.dto.GroupResponseDto;
import com.hangout.backend.group.service.GroupDiscoveryService;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GroupDiscoveryController {

    private final GroupDiscoveryService groupDiscoveryService;

    /** GET /api/groups/public?name=book -> browse or search PUBLIC groups. name is optional. */
    @GetMapping("api/groups/public")
    public ResponseEntity<Page<GroupResponseDto>> searchPublicGroups(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(groupDiscoveryService.searchPublicGroups(name, pageable));
    }

    /** GET /api/groups/me -> groups the current user actually belongs to. */
    @GetMapping("api/groups/me")
    public ResponseEntity<List<GroupResponseDto>> getMyGroups(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(groupDiscoveryService.getMyGroups(currentUser.getId()));
    }
}