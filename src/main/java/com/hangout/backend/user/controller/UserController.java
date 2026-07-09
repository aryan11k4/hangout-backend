package com.hangout.backend.user.controller;

import com.hangout.backend.user.dto.UserSummaryResponse;
import com.hangout.backend.user.mapper.UserMapper;
import com.hangout.backend.user.repository.UserRepository;
import com.hangout.backend.security.principal.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Minimal controller that only exists to prove the JWT pipeline end-to-end. */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @GetMapping("/me")
    public UserSummaryResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        var user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        return userMapper.toSummary(user);
    }
}
