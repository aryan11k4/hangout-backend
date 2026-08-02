package com.hangout.backend.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public class PrivateMessageRequestDto {
    private UUID senderId;

    @NotBlank
    @Size(max = 200, min = 1)
    private String content;
}
