package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewOutcome;
import jakarta.validation.constraints.NotNull;

public record UpdateInterviewOutcomeRequest(
    @NotNull InterviewOutcome outcome
) {
}
