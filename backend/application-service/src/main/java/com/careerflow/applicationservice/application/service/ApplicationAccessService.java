package com.careerflow.applicationservice.application.service;

import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.repository.ApplicationRepository;
import com.careerflow.applicationservice.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ApplicationAccessService {

    private final ApplicationRepository applicationRepository;

    public ApplicationAccessService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application getOwnedApplication(String userId, UUID applicationId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
    }
}
