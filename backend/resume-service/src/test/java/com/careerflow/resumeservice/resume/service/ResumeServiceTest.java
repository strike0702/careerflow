package com.careerflow.resumeservice.resume.service;

import com.careerflow.resumeservice.AbstractIntegrationTest;
import com.careerflow.resumeservice.events.outbox.OutboxEventRepository;
import com.careerflow.resumeservice.resume.dto.CreateResumeRequest;
import com.careerflow.resumeservice.resume.model.Resume;
import com.careerflow.resumeservice.resume.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class ResumeServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ResumeService resumeService;

    @Autowired
    private ResumeRepository resumeRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeEach
    void cleanUp() {
        outboxEventRepository.deleteAll();
        resumeRepository.deleteAll();
    }

    @Test
    void createResume_persistsStorageUrlAndDerivedFileName() {
        Resume created = resumeService.createResume(
            "user-a",
            new CreateResumeRequest(
                "Backend v1",
                null,
                "application/pdf",
                null,
                "https://drive.google.com/file/d/abc123/view",
                "Tailored resume",
                true
            )
        );

        assertThat(created.getStorageUrl()).isEqualTo("https://drive.google.com/file/d/abc123/view");
        assertThat(created.getFileName()).isEqualTo("view");
        assertThat(created.isPrimary()).isTrue();
        assertThat(outboxEventRepository.findAll()).hasSize(1);
    }

    @Test
    void createResume_usesProvidedFileNameWhenPresent() {
        Resume created = resumeService.createResume(
            "user-a",
            new CreateResumeRequest(
                "Backend v2",
                "custom-resume.pdf",
                null,
                null,
                "https://drive.google.com/file/d/xyz/view",
                null,
                false
            )
        );

        assertThat(created.getFileName()).isEqualTo("custom-resume.pdf");
    }
}
