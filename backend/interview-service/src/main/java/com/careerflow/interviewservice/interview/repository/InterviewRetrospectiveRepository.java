package com.careerflow.interviewservice.interview.repository;

import com.careerflow.interviewservice.interview.model.InterviewRetrospective;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InterviewRetrospectiveRepository extends JpaRepository<InterviewRetrospective, UUID> {

    Optional<InterviewRetrospective> findByInterviewIdAndUserId(UUID interviewId, String userId);
}
