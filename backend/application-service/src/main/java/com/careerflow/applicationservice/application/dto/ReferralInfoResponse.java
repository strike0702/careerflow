package com.careerflow.applicationservice.application.dto;

public record ReferralInfoResponse(
    boolean referred,
    String referrerName,
    String referrerCompanyEmail,
    String relationship
) {
}
