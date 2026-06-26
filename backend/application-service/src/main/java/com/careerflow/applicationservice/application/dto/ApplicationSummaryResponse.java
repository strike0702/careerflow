package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.application.model.ApplicationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ApplicationSummaryResponse(
    UUID id,
    String companyName,
    String jobTitle,
    ApplicationStatus status,
    LocalDate applicationDate,
    Instant createdAt
) {
}
