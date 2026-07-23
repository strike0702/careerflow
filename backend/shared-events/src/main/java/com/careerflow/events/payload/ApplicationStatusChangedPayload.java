package com.careerflow.events.payload;

import java.util.UUID;

public record ApplicationStatusChangedPayload(
    UUID applicationId,
    String previousStatus,
    String newStatus,
    String companyName,
    String jobTitle
) {
}
