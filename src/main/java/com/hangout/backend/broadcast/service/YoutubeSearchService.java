package com.hangout.backend.broadcast.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hangout.backend.broadcast.dto.YoutubeVideoDto;
import com.hangout.backend.common.exception.InvalidRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Thin wrapper around YouTube Data API v3's search.list endpoint. Costs
 * 100 quota units per call against your daily 10,000-unit free quota - so
 * this is NOT cached/rate-limited beyond whatever the frontend does by
 * debouncing search input. If quota becomes a problem, add a short-lived
 * cache here keyed on the search query.
 */
@Service
public class YoutubeSearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.youtube.api-key}")
    private String apiKey;

    public List<YoutubeVideoDto> search(String query, int limit) {
        if (query == null || query.isBlank()) {
            throw new InvalidRequestException("Search query cannot be blank");
        }

        String url = UriComponentsBuilder.fromUriString("https://www.googleapis.com/youtube/v3/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", Math.min(Math.max(limit, 1), 25))
                .queryParam("q", query)
                .queryParam("key", apiKey)
                .toUriString();

        ResponseEntity<String> response;
        try {
            // Explicit HttpEntity<Void> with an empty HttpHeaders resolves the
            // constructor ambiguity - a bare `new HttpEntity<>(null)` no longer
            // compiles in Spring Framework 7 because javac can't tell whether
            // you mean the HttpHeaders overload or the MultiValueMap overload
            // from a null literal alone.
            HttpEntity<Void> requestEntity = new HttpEntity<>(new HttpHeaders());
            response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        } catch (Exception e) {
            throw new InvalidRequestException("YouTube search failed: " + e.getMessage());
        }

        return parseResults(response.getBody());
    }

    private List<YoutubeVideoDto> parseResults(String body) {
        List<YoutubeVideoDto> results = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode items = root.path("items");

            for (JsonNode item : items) {
                String videoId = item.path("id").path("videoId").asText(null);
                if (videoId == null) continue;

                JsonNode snippet = item.path("snippet");
                String title = snippet.path("title").asText("");
                String channelName = snippet.path("channelTitle").asText("");
                String thumbnailUrl = snippet.path("thumbnails").path("medium").path("url").asText(
                        snippet.path("thumbnails").path("default").path("url").asText(""));

                results.add(new YoutubeVideoDto(videoId, title, thumbnailUrl, channelName));
            }
        } catch (Exception e) {
            throw new InvalidRequestException("Failed to parse YouTube response: " + e.getMessage());
        }
        return results;
    }
}