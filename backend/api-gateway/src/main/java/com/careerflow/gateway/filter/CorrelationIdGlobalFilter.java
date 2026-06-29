package com.careerflow.gateway.filter;

import com.careerflow.common.observability.CorrelationIdConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(CorrelationIdGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst(CorrelationIdConstants.HEADER_NAME);
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        final String correlationId = requestId;
        exchange.getResponse().getHeaders().set(CorrelationIdConstants.HEADER_NAME, correlationId);

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
            .header(CorrelationIdConstants.HEADER_NAME, correlationId)
            .build();

        long startNanos = System.nanoTime();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        return chain.filter(mutatedExchange)
            .doFinally(signalType -> {
                long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
                int status = mutatedExchange.getResponse().getStatusCode() != null
                    ? mutatedExchange.getResponse().getStatusCode().value()
                    : 0;
                log.info(
                    "method={} path={} status={} durationMs={} requestId={}",
                    mutatedRequest.getMethod(),
                    mutatedRequest.getURI().getPath(),
                    status,
                    durationMs,
                    correlationId
                );
            });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
