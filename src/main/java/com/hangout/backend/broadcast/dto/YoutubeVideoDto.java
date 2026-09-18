package com.hangout.backend.broadcast.dto;

public record YoutubeVideoDto(
        String videoId,
        String title,
        String thumbnailUrl,
        String channelName
) {}