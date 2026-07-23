package com.careerflow.resumeservice.resume.dto;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public record CreateResumeRequest(
    @NotBlank String label,
    String fileName,
    String contentType,
    Long fileSizeBytes,
    @NotBlank @URL String storageUrl,
    String notes,
    Boolean primary
) {
}
