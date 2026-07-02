package com.careerflow.gateway.exception;

import com.careerflow.common.exception.ProblemDetailFactory;
import com.careerflow.common.observability.CorrelationIdConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-2)
public class GatewayProblemDetailExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    public GatewayProblemDetailExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        HttpStatus status = resolveStatus(ex);
        ProblemDetail problemDetail = ProblemDetailFactory.create(status, resolveDetail(ex));
        applyRequestId(exchange, problemDetail);

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_PROBLEM_JSON);

        try {
            byte[] payload = objectMapper.writeValueAsBytes(problemDetail);
            DataBuffer buffer = response.bufferFactory().wrap(payload);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException jsonProcessingException) {
            return Mono.error(jsonProcessingException);
        }
    }

    private HttpStatus resolveStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            return HttpStatus.valueOf(responseStatusException.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolveDetail(Throwable ex) {
        if (ex instanceof ResponseStatusException responseStatusException) {
            String reason = responseStatusException.getReason();
            if (reason != null && !reason.isBlank()) {
                return reason;
            }
        }
        if (resolveStatus(ex).is5xxServerError()) {
            return "An unexpected error occurred";
        }
        return "Request could not be processed";
    }

    private void applyRequestId(ServerWebExchange exchange, ProblemDetail problemDetail) {
        String requestId = exchange.getResponse().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            requestId = exchange.getRequest().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);
        }
        if (requestId != null && !requestId.isBlank()) {
            problemDetail.setProperty("requestId", requestId);
        }
    }
}
