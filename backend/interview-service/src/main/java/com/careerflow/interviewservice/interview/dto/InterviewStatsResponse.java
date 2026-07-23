package com.careerflow.interviewservice.interview.dto;

import com.careerflow.interviewservice.interview.model.InterviewStatus;

import java.util.Map;

public record InterviewStatsResponse(
    long totalInterviews,
    long activeInterviews,
    long upcomingInterviews,
    long completedInterviews,
    Map<InterviewStatus, Long> interviewsByStatus
) {
}
