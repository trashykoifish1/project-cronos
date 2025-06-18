package com.timetracker.service;

import com.timetracker.dto.response.SpotifyTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class SpotifyAuthService {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    public SpotifyAuthService() {
        this.restTemplate = new RestTemplate();
    }

    public SpotifyTokenResponse exchangeCodeForTokens(String code, String redirectUri) {
        String url = "https://accounts.spotify.com/api/token";

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add Basic Auth header
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        // Create request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    SpotifyTokenResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to exchange code for tokens: " + e.getMessage(), e);
        }
    }

    public SpotifyTokenResponse refreshAccessToken(String refreshToken) {
        String url = "https://accounts.spotify.com/api/token";

        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Add Basic Auth header
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        // Create request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<SpotifyTokenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    SpotifyTokenResponse.class
            );

            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Failed to refresh access token: " + e.getMessage(), e);
        }
    }
}