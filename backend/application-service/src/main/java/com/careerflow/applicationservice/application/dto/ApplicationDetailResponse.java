package com.careerflow.applicationservice.application.dto;

import com.careerflow.applicationservice.activity.dto.ActivityResponse;
import com.careerflow.applicationservice.offer.dto.OfferResponse;

import java.util.List;

public record ApplicationDetailResponse(
    ApplicationResponse application,
    OfferResponse offer,
    List<ActivityResponse> recentActivities
) {
}
