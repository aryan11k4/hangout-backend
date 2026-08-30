package com.hangout.backend.contact.controller;

import com.hangout.backend.contact.dto.ContactUserDto;
import com.hangout.backend.contact.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users/search")
public class UserSearchController {

    private final UserSearchService userSearchService;

    @GetMapping
    public ResponseEntity<ContactUserDto> search(
            @RequestParam String username,
            @RequestParam String code) {
        return ResponseEntity.ok(userSearchService.searchByUsernameAndCode(username, code));
    }
}