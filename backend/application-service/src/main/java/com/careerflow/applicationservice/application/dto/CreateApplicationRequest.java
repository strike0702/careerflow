package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record CreateApplicationRequest(
    @NotBlank String companyName,
    @NotBlank String jobTitle,
    String location,
    @URL String jobUrl,
    @NotNull ApplicationSource source,
    ApplicationStatus status,
    LocalDate applicationDate,
    String notes,
    @Valid ReferralInfoRequest referralInfo
) {
}
