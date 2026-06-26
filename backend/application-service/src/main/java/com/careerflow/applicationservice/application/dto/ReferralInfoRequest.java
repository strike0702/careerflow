package com.careerflow.applicationservice.application.dto;

public record ReferralInfoRequest(
    boolean referred,
    String referrerName,
    String referrerCompanyEmail,
    String relationship
) {
}
