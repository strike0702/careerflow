package com.careerflow.applicationservice.offer.dto;

import com.careerflow.applicationservice.offer.model.OfferStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpsertOfferRequest(
    @DecimalMin("0") BigDecimal baseSalary,
    @DecimalMin("0") BigDecimal joiningBonus,
    @DecimalMin("0") BigDecimal annualBonus,
    @DecimalMin("0") BigDecimal stockValue,
    @NotNull @Size(min = 3, max = 3) @Pattern(regexp = "[A-Z]{3}") String currency,
    LocalDate joiningDate,
    OfferStatus offerStatus,
    String notes
) {
}
