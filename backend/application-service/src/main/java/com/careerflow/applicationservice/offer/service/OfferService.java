package com.careerflow.applicationservice.offer.service;

import com.careerflow.applicationservice.activity.model.ActivityType;
import com.careerflow.applicationservice.activity.service.ActivityService;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.service.ApplicationAccessService;
import com.careerflow.applicationservice.offer.dto.UpsertOfferRequest;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.applicationservice.offer.repository.OfferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OfferService {

    private final OfferRepository offerRepository;
    private final ApplicationAccessService applicationAccessService;
    private final ActivityService activityService;

    public OfferService(
        OfferRepository offerRepository,
        ApplicationAccessService applicationAccessService,
        ActivityService activityService
    ) {
        this.offerRepository = offerRepository;
        this.applicationAccessService = applicationAccessService;
        this.activityService = activityService;
    }

    public Offer upsertOffer(String userId, UUID applicationId, UpsertOfferRequest request) {
        Application application = applicationAccessService.getOwnedApplication(userId, applicationId);

        Offer offer = offerRepository.findByApplicationId(applicationId).orElseGet(Offer::new);
        boolean isNew = offer.getId() == null;

        offer.setApplicationId(applicationId);
        offer.setBaseSalary(request.baseSalary());
        offer.setJoiningBonus(request.joiningBonus());
        offer.setAnnualBonus(request.annualBonus());
        offer.setStockValue(request.stockValue());
        offer.setCurrency(request.currency());
        offer.setJoiningDate(request.joiningDate());
        offer.setOfferStatus(request.offerStatus());
        offer.setNotes(request.notes());

        Offer saved = offerRepository.save(offer);

        ActivityType activityType = isNew ? ActivityType.OFFER_CREATED : ActivityType.OFFER_UPDATED;
        String description = isNew
            ? "Offer added for " + application.getCompanyName() + " - " + application.getJobTitle()
            : "Offer updated for " + application.getCompanyName() + " - " + application.getJobTitle();
        activityService.logActivity(userId, applicationId, activityType, description);

        return saved;
    }

    @Transactional(readOnly = true)
    public Offer findByApplicationId(UUID applicationId) {
        return offerRepository.findByApplicationId(applicationId).orElse(null);
    }
}
