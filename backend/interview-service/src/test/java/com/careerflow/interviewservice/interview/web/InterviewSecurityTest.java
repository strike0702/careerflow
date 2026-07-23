package com.careerflow.interviewservice.interview.web;

import com.careerflow.interviewservice.AbstractIntegrationTest;
import com.careerflow.interviewservice.interview.model.Interview;
import com.careerflow.interviewservice.interview.model.InterviewMode;
import com.careerflow.interviewservice.interview.model.InterviewOutcome;
import com.careerflow.interviewservice.interview.model.InterviewStatus;
import com.careerflow.interviewservice.interview.model.RoundType;
import com.careerflow.interviewservice.interview.repository.InterviewRepository;
import com.careerflow.interviewservice.shared.client.ApplicationClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class InterviewSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InterviewRepository interviewRepository;

    @MockBean
    private ApplicationClient applicationClient;

    private UUID userBInterviewId;

    @BeforeEach
    void setUp() {
        interviewRepository.deleteAll();

        Interview interview = new Interview();
        interview.setUserId("user-b");
        interview.setApplicationId(UUID.randomUUID());
        interview.setRoundNumber(1);
        interview.setRoundType(RoundType.TECHNICAL);
        interview.setMode(InterviewMode.REMOTE);
        interview.setScheduledAt(Instant.now().plusSeconds(3600));
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setOutcome(InterviewOutcome.PENDING);
        userBInterviewId = interviewRepository.save(interview).getId();
    }

    @Test
    void userACannotAccessUserBInterview() throws Exception {
        mockMvc.perform(get("/api/v1/interviews/{id}", userBInterviewId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-a").subject("user-a"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void userBCanAccessOwnInterview() throws Exception {
        mockMvc.perform(get("/api/v1/interviews/{id}", userBInterviewId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-b").subject("user-b"))))
            .andExpect(status().isOk());
    }
}
