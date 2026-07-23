package com.careerflow.applicationservice.application.service;

import com.careerflow.common.exception.ResourceNotFoundException;
import com.careerflow.applicationservice.shared.client.ResumeClient;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ResumeValidationService {

    private final ResumeClient resumeClient;

    public ResumeValidationService(ResumeClient resumeClient) {
        this.resumeClient = resumeClient;
    }

    public void validateResumeOwnership(UUID resumeId) {
        if (resumeId == null) {
            return;
        }

        try {
            resumeClient.getResume(resumeId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Resume not found");
        } catch (FeignException ex) {
            throw new IllegalStateException("Failed to validate resume ownership", ex);
        }
    }
}
