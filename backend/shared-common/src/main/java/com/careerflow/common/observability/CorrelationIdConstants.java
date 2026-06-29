package com.careerflow.common.observability;

public final class CorrelationIdConstants {

    public static final String HEADER_NAME = "X-Request-ID";
    public static final String MDC_KEY = "requestId";

    private CorrelationIdConstants() {
    }
}
