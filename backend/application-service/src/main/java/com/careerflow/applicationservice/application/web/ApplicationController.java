package com.careerflow.applicationservice.application.web;

import com.careerflow.applicationservice.activity.model.Activity;
import com.careerflow.applicationservice.application.dto.ApplicationDetailResponse;
import com.careerflow.applicationservice.application.dto.ApplicationResponse;
import com.careerflow.applicationservice.application.dto.ApplicationSummaryResponse;
import com.careerflow.applicationservice.application.dto.CreateApplicationRequest;
import com.careerflow.applicationservice.application.dto.DashboardResponse;
import com.careerflow.applicationservice.application.dto.UpdateStatusRequest;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.application.service.ApplicationService;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.applicationservice.shared.mapper.ApplicationMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    public ApplicationController(ApplicationService applicationService, ApplicationMapper applicationMapper) {
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateApplicationRequest request
    ) {
        Application application = applicationService.createApplication(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationMapper.toResponse(application));
    }

    @GetMapping
    public Page<ApplicationSummaryResponse> listApplications(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) ApplicationStatus status,
        @RequestParam(required = false) String company,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return applicationService.listApplications(jwt.getSubject(), status, company, pageable)
            .map(applicationMapper::toSummary);
    }

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(@AuthenticationPrincipal Jwt jwt) {
        return applicationService.getDashboard(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ApplicationDetailResponse getApplication(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        String userId = jwt.getSubject();
        Application application = applicationService.getOwnedApplication(userId, id);
        Offer offer = applicationService.getOfferForApplication(id);
        List<Activity> recentActivities = applicationService.getRecentActivities(id);
        return applicationMapper.toDetailResponse(application, offer, recentActivities);
    }

    @PatchMapping("/{id}/status")
    public ApplicationDetailResponse updateStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateStatusRequest request
    ) {
        String userId = jwt.getSubject();
        Application application = applicationService.updateStatus(userId, id, request.status());
        Offer offer = applicationService.getOfferForApplication(id);
        List<Activity> recentActivities = applicationService.getRecentActivities(id);
        return applicationMapper.toDetailResponse(application, offer, recentActivities);
    }
}
