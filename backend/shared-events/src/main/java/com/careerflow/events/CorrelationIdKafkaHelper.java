package com.careerflow.events;

import com.careerflow.common.observability.CorrelationIdConstants;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;

import java.nio.charset.StandardCharsets;

public final class CorrelationIdKafkaHelper {

    private CorrelationIdKafkaHelper() {
    }

    public static void applyToHeaders(String requestId, Headers headers) {
        if (requestId != null && !requestId.isBlank()) {
            headers.add(
                CorrelationIdConstants.HEADER_NAME,
                requestId.getBytes(StandardCharsets.UTF_8)
            );
        }
    }

    public static String extractFromHeaders(Headers headers) {
        if (headers == null) {
            return null;
        }
        Header header = headers.lastHeader(CorrelationIdConstants.HEADER_NAME);
        if (header == null || header.value() == null) {
            return null;
        }
        return new String(header.value(), StandardCharsets.UTF_8);
    }

    public static void bindMdc(String requestId) {
        if (requestId != null && !requestId.isBlank()) {
            MDC.put(CorrelationIdConstants.MDC_KEY, requestId);
        }
    }

    public static void clearMdc() {
        MDC.remove(CorrelationIdConstants.MDC_KEY);
    }
}
