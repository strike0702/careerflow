package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewMode;
import com.careerflow.interviewservice.interview.model.RoundType;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

public record CreateInterviewRequest(
    @NotNull UUID applicationId,
    @NotNull RoundType roundType,
    String title,
    @NotNull InterviewMode mode,
    @NotNull Instant scheduledAt,
    Integer durationMinutes,
    String meetingLink,
    String location,
    String interviewerNames,
    String notes
) {
}
