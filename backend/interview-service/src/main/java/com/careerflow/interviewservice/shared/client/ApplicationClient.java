package com.careerflow.interviewservice.shared.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "application-service",
    url = "${careerflow.clients.application-service.url}"
)
public interface ApplicationClient {

    @GetMapping("/api/v1/applications/{id}")
    ApplicationClientResponse getApplication(@PathVariable UUID id);
}
