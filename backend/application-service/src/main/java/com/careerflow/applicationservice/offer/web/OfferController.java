package com.careerflow.applicationservice.offer.web;

import com.careerflow.applicationservice.activity.model.Activity;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.service.ApplicationService;
import com.careerflow.applicationservice.application.dto.ApplicationDetailResponse;
import com.careerflow.applicationservice.offer.dto.UpsertOfferRequest;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.applicationservice.offer.service.OfferService;
import com.careerflow.applicationservice.shared.mapper.ApplicationMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class OfferController {

    private final OfferService offerService;
    private final ApplicationService applicationService;
    private final ApplicationMapper applicationMapper;

    public OfferController(
        OfferService offerService,
        ApplicationService applicationService,
        ApplicationMapper applicationMapper
    ) {
        this.offerService = offerService;
        this.applicationService = applicationService;
        this.applicationMapper = applicationMapper;
    }

    @PutMapping("/{id}/offer")
    public ApplicationDetailResponse upsertOffer(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpsertOfferRequest request
    ) {
        String userId = jwt.getSubject();
        offerService.upsertOffer(userId, id, request);
        Application application = applicationService.getOwnedApplication(userId, id);
        Offer offer = applicationService.getOfferForApplication(id);
        List<Activity> recentActivities = applicationService.getRecentActivities(id);
        return applicationMapper.toDetailResponse(application, offer, recentActivities);
    }
}
