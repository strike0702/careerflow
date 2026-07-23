package com.careerflow.interviewservice.shared.client;

import com.careerflow.common.observability.CorrelationIdConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            template.header("Authorization", "Bearer " + jwt.getTokenValue());
        }

        String requestId = MDC.get(CorrelationIdConstants.MDC_KEY);
        if (requestId != null && !requestId.isBlank()) {
            template.header(CorrelationIdConstants.HEADER_NAME, requestId);
        }
    }
}
