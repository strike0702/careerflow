package com.careerflow.resumeservice.resume.dto;

public record UpdateResumeRequest(
    String label,
    String notes,
    Boolean primary
) {
}
