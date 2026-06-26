package com.careerflow.applicationservice.activity.service;

import com.careerflow.applicationservice.activity.model.Activity;
import com.careerflow.applicationservice.activity.model.ActivityType;
import com.careerflow.applicationservice.activity.repository.ActivityRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    private static final int DEFAULT_RECENT_ACTIVITY_LIMIT = 10;

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public Activity logActivity(String userId, UUID applicationId, ActivityType type, String description) {
        Activity activity = new Activity();
        activity.setUserId(userId);
        activity.setApplicationId(applicationId);
        activity.setType(type);
        activity.setDescription(description);
        return activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<Activity> getUserTimeline(String userId, int limit) {
        return activityRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }

    @Transactional(readOnly = true)
    public List<Activity> getRecentForApplication(UUID applicationId) {
        return activityRepository.findByApplicationIdOrderByCreatedAtDesc(
            applicationId,
            PageRequest.of(0, DEFAULT_RECENT_ACTIVITY_LIMIT)
        );
    }
}
