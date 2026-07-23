package com.careerflow.interviewservice.interview.web;

import com.careerflow.interviewservice.interview.dto.CreateInterviewRequest;
import com.careerflow.interviewservice.interview.dto.InterviewResponse;
import com.careerflow.interviewservice.interview.dto.InterviewStatsResponse;
import com.careerflow.interviewservice.interview.dto.RetrospectiveResponse;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewOutcomeRequest;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewRequest;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewStatusRequest;
import com.careerflow.interviewservice.interview.dto.UpsertRetrospectiveRequest;
import com.careerflow.interviewservice.interview.model.Interview;
import com.careerflow.interviewservice.interview.model.InterviewRetrospective;
import com.careerflow.interviewservice.interview.model.InterviewStatus;
import com.careerflow.interviewservice.interview.service.InterviewService;
import com.careerflow.interviewservice.shared.mapper.InterviewMapper;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;
    private final InterviewMapper interviewMapper;

    public InterviewController(InterviewService interviewService, InterviewMapper interviewMapper) {
        this.interviewService = interviewService;
        this.interviewMapper = interviewMapper;
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> scheduleInterview(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody CreateInterviewRequest request
    ) {
        Interview interview = interviewService.scheduleInterview(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(interviewMapper.toResponse(interview));
    }

    @GetMapping
    public Page<InterviewResponse> listInterviews(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(required = false) UUID applicationId,
        @RequestParam(required = false) InterviewStatus status,
        @PageableDefault(size = 20) Pageable pageable
    ) {
        return interviewService.listInterviews(jwt.getSubject(), applicationId, status, pageable)
            .map(interviewMapper::toResponse);
    }

    @GetMapping("/stats")
    public InterviewStatsResponse getStats(@AuthenticationPrincipal Jwt jwt) {
        return interviewService.getStats(jwt.getSubject());
    }

    @GetMapping("/{id}")
    public InterviewResponse getInterview(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        return interviewMapper.toResponse(interviewService.getOwnedInterview(jwt.getSubject(), id));
    }

    @PutMapping("/{id}")
    public InterviewResponse updateInterview(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewRequest request
    ) {
        return interviewMapper.toResponse(interviewService.updateInterview(jwt.getSubject(), id, request));
    }

    @PatchMapping("/{id}/status")
    public InterviewResponse updateStatus(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewStatusRequest request
    ) {
        return interviewMapper.toResponse(interviewService.updateStatus(jwt.getSubject(), id, request));
    }

    @PatchMapping("/{id}/outcome")
    public InterviewResponse updateOutcome(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpdateInterviewOutcomeRequest request
    ) {
        return interviewMapper.toResponse(interviewService.updateOutcome(jwt.getSubject(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInterview(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        interviewService.deleteInterview(jwt.getSubject(), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/retrospective")
    public RetrospectiveResponse upsertRetrospective(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id,
        @Valid @RequestBody UpsertRetrospectiveRequest request
    ) {
        InterviewRetrospective retrospective = interviewService.upsertRetrospective(jwt.getSubject(), id, request);
        return interviewMapper.toRetrospectiveResponse(retrospective);
    }

    @GetMapping("/{id}/retrospective")
    public RetrospectiveResponse getRetrospective(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID id
    ) {
        return interviewMapper.toRetrospectiveResponse(interviewService.getRetrospective(jwt.getSubject(), id));
    }
}
