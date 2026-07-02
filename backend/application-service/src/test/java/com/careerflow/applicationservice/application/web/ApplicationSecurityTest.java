package com.careerflow.applicationservice.application.web;

import com.careerflow.applicationservice.AbstractIntegrationTest;
import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationSource;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import com.careerflow.applicationservice.application.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ApplicationSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationRepository applicationRepository;

    private UUID userBApplicationId;

    @BeforeEach
    void setUp() {
        applicationRepository.deleteAll();

        Application application = new Application();
        application.setUserId("user-b");
        application.setCompanyName("Google");
        application.setJobTitle("Engineer");
        application.setSource(ApplicationSource.LINKEDIN);
        application.setStatus(ApplicationStatus.APPLIED);
        userBApplicationId = applicationRepository.save(application).getId();
    }

    @Test
    void userACannotAccessUserBApplication() throws Exception {
        mockMvc.perform(get("/api/v1/applications/{id}", userBApplicationId)
                .header("X-Request-ID", "security-test-request-id")
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-a").subject("user-a"))))
            .andExpect(status().isNotFound())
            .andExpect(header().string("X-Request-ID", "security-test-request-id"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.title").value("Not Found"))
            .andExpect(jsonPath("$.requestId").value("security-test-request-id"));
    }

    @Test
    void userBCanAccessOwnApplication() throws Exception {
        mockMvc.perform(get("/api/v1/applications/{id}", userBApplicationId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-b").subject("user-b"))))
            .andExpect(status().isOk());
    }
}
