package com.careerflow.applicationservice.activity.dto;

import com.careerflow.applicationservice.activity.model.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
    UUID id,
    UUID applicationId,
    ActivityType type,
    String description,
    Instant createdAt
) {
}
