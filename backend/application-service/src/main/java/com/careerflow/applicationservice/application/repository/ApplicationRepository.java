package com.careerflow.applicationservice.application.repository;

import com.careerflow.applicationservice.application.model.Application;
import com.careerflow.applicationservice.application.model.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Optional<Application> findByIdAndUserId(UUID id, String userId);

    Page<Application> findByUserId(String userId, Pageable pageable);

    Page<Application> findByUserIdAndStatus(String userId, ApplicationStatus status, Pageable pageable);

    Page<Application> findByUserIdAndCompanyNameContainingIgnoreCase(String userId, String companyName, Pageable pageable);

    Page<Application> findByUserIdAndStatusAndCompanyNameContainingIgnoreCase(
        String userId,
        ApplicationStatus status,
        String companyName,
        Pageable pageable
    );

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, ApplicationStatus status);

    @Query("""
        SELECT a.status, COUNT(a)
        FROM Application a
        WHERE a.userId = :userId
        GROUP BY a.status
        """)
    List<Object[]> countGroupedByStatus(@Param("userId") String userId);

    @Query("""
        SELECT COUNT(a)
        FROM Application a
        WHERE a.userId = :userId
          AND a.status IN :statuses
        """)
    long countByUserIdAndStatusIn(@Param("userId") String userId, @Param("statuses") List<ApplicationStatus> statuses);
}
