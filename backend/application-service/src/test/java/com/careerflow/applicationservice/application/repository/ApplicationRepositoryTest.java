package com.careerflow.applicationservice.application.repository;

import com.careerflow.applicationservice.AbstractIntegrationTest;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ApplicationRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationRepository applicationRepository;

    @BeforeEach
    void cleanUp() {
        applicationRepository.deleteAll();
    }

    @Test
    void findByUserIdAndStatusAndCompanyNameContainingIgnoreCase_filtersResults() {
        Application google = createApplication("user-a", "Google", ApplicationStatus.INTERVIEWING);
        createApplication("user-a", "Amazon", ApplicationStatus.APPLIED);
        createApplication("user-b", "Google", ApplicationStatus.INTERVIEWING);

        Page<Application> results = applicationRepository.findByUserIdAndStatusAndCompanyNameContainingIgnoreCase(
            "user-a",
            ApplicationStatus.INTERVIEWING,
            "goog",
            PageRequest.of(0, 20)
        );

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().getFirst().getId()).isEqualTo(google.getId());
    }

    @Test
    void countGroupedByStatus_returnsAggregates() {
        createApplication("user-a", "Google", ApplicationStatus.WISHLIST);
        createApplication("user-a", "Stripe", ApplicationStatus.APPLIED);
        createApplication("user-a", "Meta", ApplicationStatus.REJECTED);

        List<Object[]> grouped = applicationRepository.countGroupedByStatus("user-a");

        assertThat(grouped).hasSize(3);
    }

    private Application createApplication(String userId, String companyName, ApplicationStatus status) {
        Application application = new Application();
        application.setUserId(userId);
        application.setCompanyName(companyName);
        application.setJobTitle("Software Engineer");
        application.setSource(ApplicationSource.LINKEDIN);
        application.setStatus(status);
        return applicationRepository.save(application);
    }
}
