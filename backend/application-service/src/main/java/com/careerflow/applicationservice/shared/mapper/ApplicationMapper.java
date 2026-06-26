package com.careerflow.applicationservice.shared.mapper;

import com.careerflow.applicationservice.activity.model.Activity;
import com.careerflow.applicationservice.application.dto.ApplicationDetailResponse;
import com.careerflow.applicationservice.application.dto.ApplicationResponse;
import com.careerflow.applicationservice.application.dto.ApplicationSummaryResponse;
import com.careerflow.applicationservice.application.dto.ReferralInfoResponse;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ReferralInfo;
import com.careerflow.applicationservice.activity.dto.ActivityResponse;
import com.careerflow.applicationservice.offer.dto.OfferResponse;
import com.careerflow.applicationservice.offer.model.Offer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationMapper {

    public ApplicationSummaryResponse toSummary(Application application) {
        return new ApplicationSummaryResponse(
            application.getId(),
            application.getCompanyName(),
            application.getJobTitle(),
            application.getStatus(),
            application.getApplicationDate(),
            application.getCreatedAt()
        );
    }

    public ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
            application.getId(),
            application.getCompanyName(),
            application.getJobTitle(),
            application.getLocation(),
            application.getJobUrl(),
            application.getSource(),
            application.getStatus(),
            application.getApplicationDate(),
            application.getNotes(),
            toReferralResponse(application.getReferralInfo()),
            application.getCreatedAt(),
            application.getUpdatedAt(),
            application.getVersion()
        );
    }

    public ApplicationDetailResponse toDetailResponse(
        Application application,
        Offer offer,
        List<Activity> recentActivities
    ) {
        return new ApplicationDetailResponse(
            toResponse(application),
            offer != null ? toOfferResponse(offer) : null,
            recentActivities.stream().map(this::toActivityResponse).toList()
        );
    }

    public ReferralInfoResponse toReferralResponse(ReferralInfo referralInfo) {
        if (referralInfo == null) {
            return new ReferralInfoResponse(false, null, null, null);
        }
        return new ReferralInfoResponse(
            referralInfo.isReferred(),
            referralInfo.getReferrerName(),
            referralInfo.getReferrerCompanyEmail(),
            referralInfo.getRelationship()
        );
    }

    public OfferResponse toOfferResponse(Offer offer) {
        return new OfferResponse(
            offer.getId(),
            offer.getApplicationId(),
            offer.getBaseSalary(),
            offer.getJoiningBonus(),
            offer.getAnnualBonus(),
            offer.getStockValue(),
            offer.getCurrency(),
            offer.getJoiningDate(),
            offer.getOfferStatus(),
            offer.getNotes(),
            offer.getCreatedAt()
        );
    }

    public ActivityResponse toActivityResponse(Activity activity) {
        return new ActivityResponse(
            activity.getId(),
            activity.getApplicationId(),
            activity.getType(),
            activity.getDescription(),
            activity.getCreatedAt()
        );
    }
}
