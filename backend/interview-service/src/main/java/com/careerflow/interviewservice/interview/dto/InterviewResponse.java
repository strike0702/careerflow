package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewMode;
import com.careerflow.interviewservice.interview.model.InterviewOutcome;
import com.careerflow.interviewservice.interview.model.InterviewStatus;
import com.careerflow.interviewservice.interview.model.RoundType;

import java.time.Instant;
import java.util.UUID;

public record InterviewResponse(
    UUID id,
    UUID applicationId,
    int roundNumber,
    RoundType roundType,
    String title,
    InterviewMode mode,
    Instant scheduledAt,
    Integer durationMinutes,
    String meetingLink,
    String location,
    String interviewerNames,
    InterviewStatus status,
    InterviewOutcome outcome,
    String notes,
    Instant createdAt,
    Instant updatedAt,
    Long version
) {
}
