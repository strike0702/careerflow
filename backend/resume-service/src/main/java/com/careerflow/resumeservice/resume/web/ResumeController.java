package com.careerflow.resumeservice.resume.web;

import com.careerflow.resumeservice.resume.dto.CreateResumeRequest;
import com.careerflow.resumeservice.resume.dto.ResumeResponse;
import com.careerflow.resumeservice.resume.dto.UpdateResumeRequest;
import com.careerflow.resumeservice.resume.model.Resume;
import com.careerflow.resumeservice.resume.service.ResumeService;
import com.careerflow.resumeservice.shared.mapper.ResumeMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;
    private final ResumeMapper resumeMapper;

    public ResumeController(ResumeService resumeService, ResumeMapper resumeMapper) {
        this.resumeService = resumeService;
        this.resumeMapper = resumeMapper;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> createResume(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateResumeRequest request
    ) {
        Resume resume = resumeService.createResume(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(resumeMapper.toResponse(resume));
    }

    @GetMapping
    public List<ResumeResponse> listResumes(@AuthenticationPrincipal Jwt jwt) {
        return resumeService.listResumes(jwt.getSubject()).stream()
            .map(resumeMapper::toResponse)
            .toList();
    }

    @GetMapping("/{id}")
    public ResumeResponse getResume(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        return resumeMapper.toResponse(resumeService.getOwnedResume(jwt.getSubject(), id));
    }

    @PutMapping("/{id}")
    public ResumeResponse updateResume(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateResumeRequest request
    ) {
        return resumeMapper.toResponse(resumeService.updateResume(jwt.getSubject(), id, request));
    }

    @PutMapping("/{id}/primary")
    public ResumeResponse setPrimary(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        return resumeMapper.toResponse(resumeService.setPrimary(jwt.getSubject(), id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        resumeService.deleteResume(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }
}
