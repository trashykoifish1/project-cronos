package com.timetracker.controller;

import com.timetracker.dto.response.ApiResponse;
import com.timetracker.dto.response.VersionInfo;
import com.timetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "Application health and status endpoints")
public class HealthController {

    private final UserService userService;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${app.name:Time Tracker}")
    private String appName;

    @Value("${app.mode:local}")
    private String appMode;

    @GetMapping
    @Operation(summary = "Health check", description = "Basic health check endpoint")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = Map.of(
                "status", "UP",
                "timestamp", Instant.now(),
                "service", appName,
                "version", appVersion,
                "mode", appMode
        );
        return ResponseEntity.ok(ApiResponse.success(healthData));
    }

    @GetMapping("/version")
    @Operation(summary = "Version information", description = "Get application version and environment information")
    public ResponseEntity<ApiResponse<VersionInfo>> version() {
        VersionInfo versionInfo = VersionInfo.builder()
                .appName(appName)
                .version(appVersion)
                .mode(appMode)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.ok(ApiResponse.success(versionInfo));
    }

    @GetMapping("/status")
    @Operation(summary = "Detailed status", description = "Get detailed application status including database connectivity")
    public ResponseEntity<ApiResponse<Map<String, Object>>> status() {
        try {
            // Test database connectivity by getting current user
            var user = userService.getCurrentUser();

            Map<String, Object> statusData = Map.of(
                    "status", "HEALTHY",
                    "timestamp", Instant.now(),
                    "database", "CONNECTED",
                    "userService", "OPERATIONAL",
                    "currentUser", user.getEmail(),
                    "version", appVersion,
                    "mode", appMode
            );

            return ResponseEntity.ok(ApiResponse.success(statusData));

        } catch (Exception e) {
            Map<String, Object> statusData = Map.of(
                    "status", "DEGRADED",
                    "timestamp", Instant.now(),
                    "database", "ERROR",
                    "error", e.getMessage(),
                    "version", appVersion,
                    "mode", appMode
            );

            return ResponseEntity.status(503)
                    .body(ApiResponse.error("Service degraded: " + e.getMessage()));
        }
    }

    @GetMapping("/ping")
    @Operation(summary = "Ping endpoint", description = "Simple ping endpoint for monitoring")
    public ResponseEntity<ApiResponse<String>> ping() {
        return ResponseEntity.ok(ApiResponse.success("pong"));
    }
}