package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.application.model.ApplicationStatus;

import java.util.Map;

public record DashboardResponse(
    long totalApplications,
    long activeInterviews,
    long offersReceived,
    long rejections,
    double responseRate,
    Map<ApplicationStatus, Long> applicationsByStatus
) {
}
