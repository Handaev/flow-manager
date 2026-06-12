package org.example.flowmanager.api.entity;

import lombok.Getter;

@Getter
public enum StatusFile {

    IN_PROCESSING("processing"),
    SUCCESS("success"),
    FAILED("failed");

    private final String status;

    StatusFile(String status) {
        this.status = status;
    }
}