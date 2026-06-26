package com.careerflow.applicationservice.application.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReferralInfo {

    @Column(name = "referred", nullable = false)
    private boolean referred;

    @Column(name = "referrer_name")
    private String referrerName;

    @Column(name = "referrer_company_email")
    private String referrerCompanyEmail;

    @Column(name = "relationship")
    private String relationship;
}
