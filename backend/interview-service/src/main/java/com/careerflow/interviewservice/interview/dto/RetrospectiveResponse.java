package com.careerflow.interviewservice.interview.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record RetrospectiveResponse(
    UUID id,
    UUID interviewId,
    String whatWentWell,
    String whatToImprove,
    String questionsAsked,
    Integer selfRating,
    String followUpActions,
    Instant createdAt,
    Instant updatedAt
) {
}
