package com.hangout.backend.broadcast.controller;

import com.hangout.backend.broadcast.dto.YoutubeVideoDto;
import com.hangout.backend.broadcast.service.YoutubeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class YoutubeSearchController {

    private final YoutubeSearchService youtubeSearchService;

    @GetMapping("api/youtube/search")
    public ResponseEntity<List<YoutubeVideoDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(youtubeSearchService.search(q, limit));
    }
}