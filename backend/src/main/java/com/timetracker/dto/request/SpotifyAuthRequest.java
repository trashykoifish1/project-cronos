package com.timetracker.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyAuthRequest {
    private String code;
    private String redirectUri;
}