package com.careerflow.events.payload;

import java.util.UUID;

public record ResumeDeletedPayload(
    UUID resumeId
) {
}
