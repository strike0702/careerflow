package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.application.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
    @NotNull ApplicationStatus status
) {
}
