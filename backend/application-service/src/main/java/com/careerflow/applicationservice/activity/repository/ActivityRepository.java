package com.careerflow.applicationservice.activity.repository;

import com.careerflow.applicationservice.activity.model.Activity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityRepository extends JpaRepository<Activity, UUID> {

    List<Activity> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    List<Activity> findByApplicationIdOrderByCreatedAtDesc(UUID applicationId, Pageable pageable);
}
