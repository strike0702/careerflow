package com.careerflow.resumeservice.resume.web;

import com.careerflow.resumeservice.AbstractIntegrationTest;
import com.careerflow.resumeservice.resume.model.ParseStatus;
import com.careerflow.resumeservice.resume.model.Resume;
import com.careerflow.resumeservice.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class ResumeSecurityTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ResumeRepository resumeRepository;

    private UUID userBResumeId;

    @BeforeEach
    void setUp() {
        resumeRepository.deleteAll();

        Resume resume = new Resume();
        resume.setUserId("user-b");
        resume.setLabel("Backend v1");
        resume.setVersionNo(1);
        resume.setFileName("resume.pdf");
        resume.setStorageUrl("mock://local-storage/resumes/user-b/resume.pdf");
        resume.setStorageKey("resumes/user-b/resume.pdf");
        resume.setPrimary(true);
        resume.setParseStatus(ParseStatus.NOT_PARSED);
        userBResumeId = resumeRepository.save(resume).getId();
    }

    @Test
    void userACannotAccessUserBResume() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{id}", userBResumeId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-a").subject("user-a"))))
            .andExpect(status().isNotFound());
    }

    @Test
    void userBCanAccessOwnResume() throws Exception {
        mockMvc.perform(get("/api/v1/resumes/{id}", userBResumeId)
                .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.tokenValue("user-b").subject("user-b"))))
            .andExpect(status().isOk());
    }
}
