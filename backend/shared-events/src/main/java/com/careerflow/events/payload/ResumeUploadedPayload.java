package com.careerflow.events.payload;

import java.util.UUID;

public record ResumeUploadedPayload(
    UUID resumeId,
    String label,
    String fileName,
    String contentType,
    Long fileSizeBytes,
    String storageUrl,
    int versionNo
) {
}
