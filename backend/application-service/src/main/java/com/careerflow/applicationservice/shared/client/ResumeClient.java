package com.careerflow.applicationservice.shared.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "resume-service",
    url = "${careerflow.clients.resume-service.url}"
)
public interface ResumeClient {

    @GetMapping("/api/v1/resumes/{id}")
    ResumeClientResponse getResume(@PathVariable UUID id);
}
