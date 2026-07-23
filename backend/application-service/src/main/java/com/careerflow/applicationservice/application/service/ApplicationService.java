package com.careerflow.applicationservice.application.service;

import com.careerflow.applicationservice.activity.model.ActivityType;
import com.careerflow.applicationservice.activity.service.ActivityService;
import com.careerflow.applicationservice.application.dto.CreateApplicationRequest;
import com.careerflow.applicationservice.application.dto.DashboardResponse;
import com.careerflow.applicationservice.application.dto.ReferralInfoRequest;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.application.model.ReferralInfo;
import com.careerflow.applicationservice.application.repository.ApplicationRepository;
import com.careerflow.applicationservice.activity.model.Activity;
import com.careerflow.applicationservice.offer.model.Offer;
import com.careerflow.applicationservice.events.outbox.OutboxWriter;
import com.careerflow.applicationservice.offer.service.OfferService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ApplicationService {

    private static final List<ApplicationStatus> RESPONSE_STATUSES = List.of(
        ApplicationStatus.ASSESSMENT,
        ApplicationStatus.INTERVIEWING,
        ApplicationStatus.OFFERED,
        ApplicationStatus.HIRED,
        ApplicationStatus.REJECTED
    );

    private final ApplicationRepository applicationRepository;
    private final ApplicationAccessService applicationAccessService;
    private final ActivityService activityService;
    private final OfferService offerService;
    private final OutboxWriter outboxWriter;
    private final ResumeValidationService resumeValidationService;

    public ApplicationService(
        ApplicationRepository applicationRepository,
        ApplicationAccessService applicationAccessService,
        ActivityService activityService,
        OfferService offerService,
        OutboxWriter outboxWriter,
        ResumeValidationService resumeValidationService
    ) {
        this.applicationRepository = applicationRepository;
        this.applicationAccessService = applicationAccessService;
        this.activityService = activityService;
        this.offerService = offerService;
        this.outboxWriter = outboxWriter;
        this.resumeValidationService = resumeValidationService;
    }

    public Application createApplication(String userId, CreateApplicationRequest request) {
        resumeValidationService.validateResumeOwnership(request.resumeId());

        Application application = new Application();
        application.setUserId(userId);
        application.setCompanyName(request.companyName());
        application.setJobTitle(request.jobTitle());
        application.setLocation(request.location());
        application.setJobUrl(request.jobUrl());
        application.setSource(request.source());
        application.setStatus(request.status() != null ? request.status() : ApplicationStatus.WISHLIST);
        application.setApplicationDate(request.applicationDate());
        application.setNotes(request.notes());
        application.setResumeId(request.resumeId());
        application.setReferralInfo(mapReferralInfo(request.referralInfo()));

        Application saved = applicationRepository.save(application);

        activityService.logActivity(
            userId,
            saved.getId(),
            ActivityType.APPLICATION_CREATED,
            "Application created for " + saved.getCompanyName() + " - " + saved.getJobTitle()
        );

        outboxWriter.writeApplicationCreated(saved, userId);

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Application> listApplications(
        String userId,
        ApplicationStatus status,
        String company,
        Pageable pageable
    ) {
        boolean hasStatus = status != null;
        boolean hasCompany = company != null && !company.isBlank();

        if (hasStatus && hasCompany) {
            return applicationRepository.findByUserIdAndStatusAndCompanyNameContainingIgnoreCase(
                userId, status, company.trim(), pageable
            );
        }
        if (hasStatus) {
            return applicationRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        if (hasCompany) {
            return applicationRepository.findByUserIdAndCompanyNameContainingIgnoreCase(
                userId, company.trim(), pageable
            );
        }
        return applicationRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Application getOwnedApplication(String userId, UUID applicationId) {
        return applicationAccessService.getOwnedApplication(userId, applicationId);
    }

    @Transactional(readOnly = true)
    public Offer getOfferForApplication(UUID applicationId) {
        return offerService.findByApplicationId(applicationId);
    }

    @Transactional(readOnly = true)
    public List<Activity> getRecentActivities(UUID applicationId) {
        return activityService.getRecentForApplication(applicationId);
    }

    public Application updateStatus(String userId, UUID applicationId, ApplicationStatus newStatus) {
        Application application = applicationAccessService.getOwnedApplication(userId, applicationId);
        ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(newStatus);
        Application saved = applicationRepository.save(application);

        activityService.logActivity(
            userId,
            applicationId,
            ActivityType.STATUS_CHANGED,
            "Status changed from " + previousStatus + " to " + newStatus
                + " for " + application.getCompanyName() + " - " + application.getJobTitle()
        );

        outboxWriter.writeApplicationStatusChanged(saved, previousStatus, userId);

        return saved;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String userId) {
        long totalApplications = applicationRepository.countByUserId(userId);
        long activeInterviews = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.INTERVIEWING);
        long offersReceived = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.OFFERED);
        long rejections = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.REJECTED);

        long wishlistCount = applicationRepository.countByUserIdAndStatus(userId, ApplicationStatus.WISHLIST);
        long respondedCount = applicationRepository.countByUserIdAndStatusIn(userId, RESPONSE_STATUSES);
        long submittedCount = totalApplications - wishlistCount;

        double responseRate = submittedCount > 0
            ? (double) respondedCount / submittedCount * 100.0
            : 0.0;

        Map<ApplicationStatus, Long> applicationsByStatus = new EnumMap<>(ApplicationStatus.class);
        for (Object[] row : applicationRepository.countGroupedByStatus(userId)) {
            applicationsByStatus.put((ApplicationStatus) row[0], (Long) row[1]);
        }

        return new DashboardResponse(
            totalApplications,
            activeInterviews,
            offersReceived,
            rejections,
            responseRate,
            applicationsByStatus
        );
    }

    private ReferralInfo mapReferralInfo(ReferralInfoRequest request) {
        ReferralInfo referralInfo = new ReferralInfo();
        if (request == null) {
            referralInfo.setReferred(false);
            return referralInfo;
        }
        referralInfo.setReferred(request.referred());
        referralInfo.setReferrerName(request.referrerName());
        referralInfo.setReferrerCompanyEmail(request.referrerCompanyEmail());
        referralInfo.setRelationship(request.relationship());
        return referralInfo;
    }
}
