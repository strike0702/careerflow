package com.careerflow.events.payload;

import java.util.UUID;

public record InterviewCompletedPayload(
    UUID interviewId,
    UUID applicationId,
    String outcome,
    String completedAt
) {
}
