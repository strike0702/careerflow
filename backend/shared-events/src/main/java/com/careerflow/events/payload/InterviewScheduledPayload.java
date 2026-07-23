package com.careerflow.events.payload;

import java.util.UUID;

public record InterviewScheduledPayload(
    UUID interviewId,
    UUID applicationId,
    String roundType,
    String scheduledAt,
    String mode
) {
}
