package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ApplicationResponse(
    UUID id,
    String companyName,
    String jobTitle,
    String location,
    String jobUrl,
    ApplicationSource source,
    ApplicationStatus status,
    LocalDate applicationDate,
    String notes,
    ReferralInfoResponse referralInfo,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {
}
