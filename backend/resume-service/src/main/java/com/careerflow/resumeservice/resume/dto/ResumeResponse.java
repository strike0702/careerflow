package com.careerflow.resumeservice.resume.dto;

import com.careerflow.resumeservice.resume.model.ParseStatus;

import java.time.Instant;
import java.util.UUID;

public record ResumeResponse(
    UUID id,
    String label,
    int versionNo,
    String fileName,
    String contentType,
    Long fileSizeBytes,
    String storageUrl,
    boolean primary,
    ParseStatus parseStatus,
    Instant parsedAt,
    String parseError,
    String notes,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {
}
