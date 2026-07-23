package com.careerflow.interviewservice.interview.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpsertRetrospectiveRequest(
    String whatWentWell,
    String whatToImprove,
    String questionsAsked,
    @Min(1) @Max(5) Integer selfRating,
    String followUpActions
) {
}
