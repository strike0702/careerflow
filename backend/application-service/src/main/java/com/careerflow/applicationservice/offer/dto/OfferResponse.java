package com.careerflow.applicationservice.offer.dto;

import com.careerflow.applicationservice.offer.model.OfferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record OfferResponse(
    UUID id,
    UUID applicationId,
    BigDecimal baseSalary,
    BigDecimal joiningBonus,
    BigDecimal annualBonus,
    BigDecimal stockValue,
    String currency,
    LocalDate joiningDate,
    OfferStatus offerStatus,
    String notes,
    Instant createdAt
) {
}
