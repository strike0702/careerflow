package com.careerflow.interviewservice.interview.service;

import com.careerflow.common.exception.ResourceNotFoundException;
import com.careerflow.interviewservice.events.outbox.OutboxWriter;
import com.careerflow.interviewservice.interview.dto.CreateInterviewRequest;
import com.careerflow.interviewservice.interview.dto.InterviewStatsResponse;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewOutcomeRequest;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewRequest;
import com.careerflow.interviewservice.interview.dto.UpdateInterviewStatusRequest;
import com.careerflow.interviewservice.interview.dto.UpsertRetrospectiveRequest;
import com.careerflow.interviewservice.interview.model.Interview;
import com.careerflow.interviewservice.interview.model.InterviewOutcome;
import com.careerflow.interviewservice.interview.model.InterviewRetrospective;
import com.careerflow.interviewservice.interview.model.InterviewStatus;
import com.careerflow.interviewservice.interview.repository.InterviewRepository;
import com.careerflow.interviewservice.interview.repository.InterviewRetrospectiveRepository;
import com.careerflow.interviewservice.shared.client.ApplicationClient;
import feign.FeignException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class InterviewService {

    private final InterviewRepository interviewRepository;
    private final InterviewRetrospectiveRepository retrospectiveRepository;
    private final ApplicationClient applicationClient;
    private final OutboxWriter outboxWriter;

    public InterviewService(
        InterviewRepository interviewRepository,
        InterviewRetrospectiveRepository retrospectiveRepository,
        ApplicationClient applicationClient,
        OutboxWriter outboxWriter
    ) {
        this.interviewRepository = interviewRepository;
        this.retrospectiveRepository = retrospectiveRepository;
        this.applicationClient = applicationClient;
        this.outboxWriter = outboxWriter;
    }

    public Interview scheduleInterview(String userId, CreateInterviewRequest request) {
        validateApplicationOwnership(request.applicationId());

        int roundNumber = interviewRepository.countByUserIdAndApplicationId(userId, request.applicationId()) + 1;

        Interview interview = new Interview();
        interview.setUserId(userId);
        interview.setApplicationId(request.applicationId());
        interview.setRoundNumber(roundNumber);
        interview.setRoundType(request.roundType());
        interview.setTitle(request.title());
        interview.setMode(request.mode());
        interview.setScheduledAt(request.scheduledAt());
        interview.setDurationMinutes(request.durationMinutes());
        interview.setMeetingLink(request.meetingLink());
        interview.setLocation(request.location());
        interview.setInterviewerNames(request.interviewerNames());
        interview.setStatus(InterviewStatus.SCHEDULED);
        interview.setOutcome(InterviewOutcome.PENDING);
        interview.setNotes(request.notes());

        Interview saved = interviewRepository.save(interview);
        outboxWriter.writeInterviewScheduled(saved, userId);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Interview> listInterviews(
        String userId,
        UUID applicationId,
        InterviewStatus status,
        Pageable pageable
    ) {
        boolean hasApplication = applicationId != null;
        boolean hasStatus = status != null;

        if (hasApplication && hasStatus) {
            return interviewRepository.findByUserIdAndApplicationIdAndStatus(userId, applicationId, status, pageable);
        }
        if (hasApplication) {
            return interviewRepository.findByUserIdAndApplicationId(userId, applicationId, pageable);
        }
        if (hasStatus) {
            return interviewRepository.findByUserIdAndStatus(userId, status, pageable);
        }
        return interviewRepository.findByUserId(userId, pageable);
    }

    @Transactional(readOnly = true)
    public Interview getOwnedInterview(String userId, UUID interviewId) {
        return interviewRepository.findByIdAndUserId(interviewId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Interview not found"));
    }

    public Interview updateInterview(String userId, UUID interviewId, UpdateInterviewRequest request) {
        Interview interview = getOwnedInterview(userId, interviewId);

        if (request.roundType() != null) {
            interview.setRoundType(request.roundType());
        }
        if (request.title() != null) {
            interview.setTitle(request.title());
        }
        if (request.mode() != null) {
            interview.setMode(request.mode());
        }
        if (request.scheduledAt() != null) {
            interview.setScheduledAt(request.scheduledAt());
        }
        if (request.durationMinutes() != null) {
            interview.setDurationMinutes(request.durationMinutes());
        }
        if (request.meetingLink() != null) {
            interview.setMeetingLink(request.meetingLink());
        }
        if (request.location() != null) {
            interview.setLocation(request.location());
        }
        if (request.interviewerNames() != null) {
            interview.setInterviewerNames(request.interviewerNames());
        }
        if (request.notes() != null) {
            interview.setNotes(request.notes());
        }

        return interviewRepository.save(interview);
    }

    public Interview updateStatus(String userId, UUID interviewId, UpdateInterviewStatusRequest request) {
        Interview interview = getOwnedInterview(userId, interviewId);
        interview.setStatus(request.status());
        Interview saved = interviewRepository.save(interview);

        if (request.status() == InterviewStatus.COMPLETED) {
            outboxWriter.writeInterviewCompleted(saved, userId);
        }

        return saved;
    }

    public Interview updateOutcome(String userId, UUID interviewId, UpdateInterviewOutcomeRequest request) {
        Interview interview = getOwnedInterview(userId, interviewId);
        interview.setOutcome(request.outcome());
        return interviewRepository.save(interview);
    }

    public void deleteInterview(String userId, UUID interviewId) {
        Interview interview = getOwnedInterview(userId, interviewId);
        interviewRepository.delete(interview);
    }

    public InterviewRetrospective upsertRetrospective(
        String userId,
        UUID interviewId,
        UpsertRetrospectiveRequest request
    ) {
        getOwnedInterview(userId, interviewId);

        InterviewRetrospective retrospective = retrospectiveRepository
            .findByInterviewIdAndUserId(interviewId, userId)
            .orElseGet(() -> {
                InterviewRetrospective created = new InterviewRetrospective();
                created.setInterviewId(interviewId);
                created.setUserId(userId);
                return created;
            });

        retrospective.setWhatWentWell(request.whatWentWell());
        retrospective.setWhatToImprove(request.whatToImprove());
        retrospective.setQuestionsAsked(request.questionsAsked());
        retrospective.setSelfRating(request.selfRating());
        retrospective.setFollowUpActions(request.followUpActions());

        return retrospectiveRepository.save(retrospective);
    }

    @Transactional(readOnly = true)
    public InterviewRetrospective getRetrospective(String userId, UUID interviewId) {
        getOwnedInterview(userId, interviewId);
        return retrospectiveRepository.findByInterviewIdAndUserId(interviewId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Retrospective not found"));
    }

    @Transactional(readOnly = true)
    public InterviewStatsResponse getStats(String userId) {
        long totalInterviews = interviewRepository.countByUserId(userId);
        long activeInterviews = interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.SCHEDULED);
        long upcomingInterviews = interviewRepository.countUpcoming(userId, Instant.now());
        long completedInterviews = interviewRepository.countByUserIdAndStatus(userId, InterviewStatus.COMPLETED);

        Map<InterviewStatus, Long> interviewsByStatus = new EnumMap<>(InterviewStatus.class);
        for (Object[] row : interviewRepository.countGroupedByStatus(userId)) {
            interviewsByStatus.put((InterviewStatus) row[0], (Long) row[1]);
        }

        return new InterviewStatsResponse(
            totalInterviews,
            activeInterviews,
            upcomingInterviews,
            completedInterviews,
            interviewsByStatus
        );
    }

    private void validateApplicationOwnership(UUID applicationId) {
        try {
            applicationClient.getApplication(applicationId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Application not found");
        } catch (FeignException ex) {
            throw new IllegalStateException("Failed to validate application ownership", ex);
        }
    }
}
