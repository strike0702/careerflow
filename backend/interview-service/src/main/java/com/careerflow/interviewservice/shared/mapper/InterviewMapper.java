package com.careerflow.interviewservice.shared.mapper;

import com.careerflow.interviewservice.interview.dto.InterviewResponse;
import com.careerflow.interviewservice.interview.dto.RetrospectiveResponse;
import com.careerflow.interviewservice.interview.model.Interview;
import com.careerflow.interviewservice.interview.model.InterviewRetrospective;
import org.springframework.stereotype.Component;

@Component
public class InterviewMapper {

    public InterviewResponse toResponse(Interview interview) {
        return new InterviewResponse(
            interview.getId(),
            interview.getApplicationId(),
            interview.getRoundNumber(),
            interview.getRoundType(),
            interview.getTitle(),
            interview.getMode(),
            interview.getScheduledAt(),
            interview.getDurationMinutes(),
            interview.getMeetingLink(),
            interview.getLocation(),
            interview.getInterviewerNames(),
            interview.getStatus(),
            interview.getOutcome(),
            interview.getNotes(),
            interview.getCreatedAt(),
            interview.getUpdatedAt(),
            interview.getVersion()
        );
    }

    public RetrospectiveResponse toRetrospectiveResponse(InterviewRetrospective retrospective) {
        return new RetrospectiveResponse(
            retrospective.getId(),
            retrospective.getInterviewId(),
            retrospective.getWhatWentWell(),
            retrospective.getWhatToImprove(),
            retrospective.getQuestionsAsked(),
            retrospective.getSelfRating(),
            retrospective.getFollowUpActions(),
            retrospective.getCreatedAt(),
            retrospective.getUpdatedAt()
        );
    }
}
