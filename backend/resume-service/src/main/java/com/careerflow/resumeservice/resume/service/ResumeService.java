package com.careerflow.resumeservice.resume.service;

import com.careerflow.common.exception.ResourceNotFoundException;
import com.careerflow.resumeservice.events.outbox.OutboxWriter;
import com.careerflow.resumeservice.resume.dto.CreateResumeRequest;
import com.careerflow.resumeservice.resume.dto.UpdateResumeRequest;
import com.careerflow.resumeservice.resume.model.ParseStatus;
import com.careerflow.resumeservice.resume.model.Resume;
import com.careerflow.resumeservice.resume.repository.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final OutboxWriter outboxWriter;

    public ResumeService(ResumeRepository resumeRepository, OutboxWriter outboxWriter) {
        this.resumeRepository = resumeRepository;
        this.outboxWriter = outboxWriter;
    }

    public Resume createResume(String userId, CreateResumeRequest request) {
        int versionNo = resumeRepository.countByUserId(userId) + 1;
        UUID resumeId = UUID.randomUUID();
        String fileName = resolveFileName(request.fileName(), request.storageUrl());
        String storageKey = "resumes/" + userId + "/" + resumeId + "/" + fileName;

        Resume resume = new Resume();
        resume.setId(resumeId);
        resume.setUserId(userId);
        resume.setLabel(request.label());
        resume.setVersionNo(versionNo);
        resume.setFileName(fileName);
        resume.setContentType(request.contentType());
        resume.setFileSizeBytes(request.fileSizeBytes());
        resume.setStorageUrl(request.storageUrl());
        resume.setStorageKey(storageKey);
        resume.setPrimary(Boolean.TRUE.equals(request.primary()) || versionNo == 1);
        resume.setParseStatus(ParseStatus.NOT_PARSED);
        resume.setNotes(request.notes());

        if (resume.isPrimary()) {
            resumeRepository.clearPrimaryForUser(userId);
        }

        Resume saved = resumeRepository.save(resume);
        outboxWriter.writeResumeUploaded(saved, userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Resume> listResumes(String userId) {
        return resumeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Resume getOwnedResume(String userId, UUID resumeId) {
        return resumeRepository.findByIdAndUserId(resumeId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    public Resume updateResume(String userId, UUID resumeId, UpdateResumeRequest request) {
        Resume resume = getOwnedResume(userId, resumeId);

        if (request.label() != null && !request.label().isBlank()) {
            resume.setLabel(request.label());
        }
        if (request.notes() != null) {
            resume.setNotes(request.notes());
        }
        if (request.primary() != null) {
            if (request.primary()) {
                resumeRepository.clearPrimaryForUser(userId);
                resume.setPrimary(true);
            } else if (resume.isPrimary()) {
                resume.setPrimary(false);
            }
        }

        return resumeRepository.save(resume);
    }

    public Resume setPrimary(String userId, UUID resumeId) {
        Resume resume = getOwnedResume(userId, resumeId);
        resumeRepository.clearPrimaryForUser(userId);
        resume.setPrimary(true);
        return resumeRepository.save(resume);
    }

    public void deleteResume(String userId, UUID resumeId) {
        Resume resume = getOwnedResume(userId, resumeId);
        resumeRepository.delete(resume);
        outboxWriter.writeResumeDeleted(resumeId, userId);
    }

    private String resolveFileName(String fileName, String storageUrl) {
        if (fileName != null && !fileName.isBlank()) {
            return fileName.trim();
        }

        try {
            String path = java.net.URI.create(storageUrl).getPath();
            if (path != null && !path.isBlank()) {
                int lastSlash = path.lastIndexOf('/');
                String segment = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
                if (!segment.isBlank()) {
                    return segment;
                }
            }
        } catch (IllegalArgumentException ignored) {
            // Fall through to default file name.
        }

        return "resume.pdf";
    }
}
