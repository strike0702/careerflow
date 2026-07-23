package com.careerflow.events.payload;

import java.util.UUID;

public record ApplicationCreatedPayload(
    UUID applicationId,
    String companyName,
    String jobTitle,
    String status,
    String location,
    String source
) {
}
