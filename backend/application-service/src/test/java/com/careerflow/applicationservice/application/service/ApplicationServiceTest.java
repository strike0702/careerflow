package com.careerflow.applicationservice.application.service;

import com.careerflow.applicationservice.AbstractIntegrationTest;
import com.careerflow.applicationservice.activity.repository.ActivityRepository;
import com.careerflow.applicationservice.application.dto.CreateApplicationRequest;
import com.careerflow.applicationservice.application.dto.DashboardResponse;
import com.careerflow.applicationservice.application.dto.ReferralInfoRequest;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.application.repository.ApplicationRepository;
import com.careerflow.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApplicationServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @BeforeEach
    void cleanUp() {
        activityRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void createApplication_persistsApplicationAndActivity() {
        CreateApplicationRequest request = new CreateApplicationRequest(
            "Stripe",
            "Staff Engineer",
            "Remote",
            "https://stripe.com/jobs/123",
            ApplicationSource.REFERRAL,
            ApplicationStatus.APPLIED,
            null,
            "Great team",
            new ReferralInfoRequest(true, "Jane", "jane@stripe.com", "Coworker")
        );

        Application created = applicationService.createApplication("user-a", request);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getUserId()).isEqualTo("user-a");
        assertThat(activityRepository.findAll()).hasSize(1);
    }

    @Test
    void updateStatus_createsStatusChangedActivity() {
        Application application = applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Google",
                "Backend Engineer",
                null,
                null,
                ApplicationSource.LINKEDIN,
                ApplicationStatus.APPLIED,
                null,
                null,
                null
            )
        );

        applicationService.updateStatus("user-a", application.getId(), ApplicationStatus.INTERVIEWING);

        assertThat(applicationRepository.findById(application.getId()).orElseThrow().getStatus())
            .isEqualTo(ApplicationStatus.INTERVIEWING);
        assertThat(activityRepository.findAll()).hasSize(2);
    }

    @Test
    void getOwnedApplication_throwsWhenApplicationBelongsToAnotherUser() {
        Application application = applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Meta",
                "Engineer",
                null,
                null,
                ApplicationSource.OTHER,
                ApplicationStatus.WISHLIST,
                null,
                null,
                null
            )
        );

        assertThatThrownBy(() -> applicationService.getOwnedApplication("user-b", application.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getDashboard_usesAggregationQueries() {
        applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Google",
                "Engineer",
                null,
                null,
                ApplicationSource.LINKEDIN,
                ApplicationStatus.WISHLIST,
                null,
                null,
                null
            )
        );
        applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Stripe",
                "Engineer",
                null,
                null,
                ApplicationSource.LINKEDIN,
                ApplicationStatus.INTERVIEWING,
                null,
                null,
                null
            )
        );
        applicationService.createApplication(
            "user-a",
            new CreateApplicationRequest(
                "Meta",
                "Engineer",
                null,
                null,
                ApplicationSource.LINKEDIN,
                ApplicationStatus.REJECTED,
                null,
                null,
                null
            )
        );

        DashboardResponse dashboard = applicationService.getDashboard("user-a");

        assertThat(dashboard.totalApplications()).isEqualTo(3);
        assertThat(dashboard.activeInterviews()).isEqualTo(1);
        assertThat(dashboard.rejections()).isEqualTo(1);
        assertThat(dashboard.applicationsByStatus()).containsKeys(
            ApplicationStatus.WISHLIST,
            ApplicationStatus.INTERVIEWING,
            ApplicationStatus.REJECTED
        );
    }
}
