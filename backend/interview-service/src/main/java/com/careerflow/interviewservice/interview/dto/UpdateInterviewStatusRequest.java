package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateInterviewStatusRequest(
    @NotNull InterviewStatus status
) {
}
