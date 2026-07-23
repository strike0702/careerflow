package com.careerflow.resumeservice.resume.repository;

import com.careerflow.resumeservice.resume.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {

    List<Resume> findByUserIdOrderByCreatedAtDesc(String userId);

    Optional<Resume> findByIdAndUserId(UUID id, String userId);

    int countByUserId(String userId);

    @Modifying
    @Query("UPDATE Resume r SET r.primary = false WHERE r.userId = :userId AND r.primary = true")
    void clearPrimaryForUser(@Param("userId") String userId);
}
