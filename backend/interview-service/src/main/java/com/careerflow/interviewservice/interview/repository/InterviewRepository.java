package com.careerflow.interviewservice.interview.repository;

import com.careerflow.interviewservice.interview.model.Interview;
import com.careerflow.interviewservice.interview.model.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {

    Optional<Interview> findByIdAndUserId(UUID id, String userId);

    Page<Interview> findByUserId(String userId, Pageable pageable);

    Page<Interview> findByUserIdAndApplicationId(String userId, UUID applicationId, Pageable pageable);

    Page<Interview> findByUserIdAndStatus(String userId, InterviewStatus status, Pageable pageable);

    Page<Interview> findByUserIdAndApplicationIdAndStatus(
        String userId,
        UUID applicationId,
        InterviewStatus status,
        Pageable pageable
    );

    int countByUserIdAndStatus(String userId, InterviewStatus status);

    long countByUserId(String userId);

    @Query("""
        SELECT COUNT(i)
        FROM Interview i
        WHERE i.userId = :userId
          AND i.status = com.careerflow.interviewservice.interview.model.InterviewStatus.SCHEDULED
          AND i.scheduledAt >= :now
        """)
    long countUpcoming(@Param("userId") String userId, @Param("now") Instant now);

    @Query("""
        SELECT i.status, COUNT(i)
        FROM Interview i
        WHERE i.userId = :userId
        GROUP BY i.status
        """)
    List<Object[]> countGroupedByStatus(@Param("userId") String userId);

    int countByUserIdAndApplicationId(String userId, UUID applicationId);
}
