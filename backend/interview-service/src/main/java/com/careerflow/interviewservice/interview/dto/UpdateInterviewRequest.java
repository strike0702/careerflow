package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewMode;
import com.careerflow.interviewservice.interview.model.RoundType;

import java.time.Instant;

public record UpdateInterviewRequest(
    RoundType roundType,
    String title,
    InterviewMode mode,
    Instant scheduledAt,
    Integer durationMinutes,
    String meetingLink,
    String location,
    String interviewerNames,
    String notes
) {
}
