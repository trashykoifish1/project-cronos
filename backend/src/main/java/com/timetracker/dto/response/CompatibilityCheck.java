package com.timetracker.dto.response;

import lombok.Data;

@Data
public class CompatibilityCheck {
    private Boolean compatible;
    private String message;
    private String recommendedVersion;

    public CompatibilityCheck(Boolean compatible, String message) {
        this.compatible = compatible;
        this.message = message;
    }
}