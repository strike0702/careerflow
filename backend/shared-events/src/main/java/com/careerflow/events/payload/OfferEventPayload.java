package com.careerflow.events.payload;

import java.util.UUID;

public record OfferEventPayload(
    UUID applicationId,
    UUID offerId,
    String baseSalary,
    String currency,
    String offerStatus,
    String joiningDate,
    String companyName,
    String jobTitle
) {
}
