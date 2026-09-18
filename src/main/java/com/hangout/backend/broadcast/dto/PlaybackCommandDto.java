package com.hangout.backend.broadcast.dto;

/** Client -> server, for PLAY/PAUSE/SEEK commands. broadcastId comes from the STOMP destination, not the payload. */
public record PlaybackCommandDto(
        double position
) {}
