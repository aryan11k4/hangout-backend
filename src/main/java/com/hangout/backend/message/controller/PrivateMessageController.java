package com.hangout.backend.message.controller;

import com.hangout.backend.message.service.PrivateMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/private-message")
public class PrivateMessageController {
    private final PrivateMessageService messageService;

    public
}
