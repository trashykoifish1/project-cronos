package com.timetracker.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class VersionInfo {
    private String appName;
    private String version;
    private String mode;
    private Instant timestamp;
    private String databaseVersion;
    private String minimumFrontendVersion;
}