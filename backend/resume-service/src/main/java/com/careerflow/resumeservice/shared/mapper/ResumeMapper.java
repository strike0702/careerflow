package com.careerflow.resumeservice.shared.mapper;

import com.careerflow.resumeservice.resume.dto.ResumeResponse;
import com.careerflow.resumeservice.resume.model.Resume;
import org.springframework.stereotype.Component;

@Component
public class ResumeMapper {

    public ResumeResponse toResponse(Resume resume) {
        return new ResumeResponse(
            resume.getId(),
            resume.getLabel(),
            resume.getVersionNo(),
            resume.getFileName(),
            resume.getContentType(),
            resume.getFileSizeBytes(),
            resume.getStorageUrl(),
            resume.isPrimary(),
            resume.getParseStatus(),
            resume.getParsedAt(),
            resume.getParseError(),
            resume.getNotes(),
            resume.getCreatedAt(),
            resume.getUpdatedAt(),
            resume.getVersion()
        );
    }
}
