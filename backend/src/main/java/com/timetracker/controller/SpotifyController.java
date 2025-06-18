package com.timetracker.controller;

import com.timetracker.dto.request.SpotifyAuthRequest;
import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.SpotifyTokenResponse;
import com.timetracker.service.SpotifyAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final SpotifyAuthService spotifyAuthService;

    @PostMapping("/token")
    public ResponseEntity<?> exchangeCodeForToken(@RequestBody SpotifyAuthRequest request) {
        try {
            SpotifyTokenResponse tokens = spotifyAuthService.exchangeCodeForTokens(
                    request.getCode(),
                    request.getRedirectUri()
            );
            return ResponseEntity.ok(ApiResponse.success(tokens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to exchange code for tokens", "message", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "refresh_token is required"));
            }

            SpotifyTokenResponse tokens = spotifyAuthService.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(ApiResponse.success(tokens));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to refresh token", "message", e.getMessage()));
        }
    }
}
