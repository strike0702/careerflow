package com.careerflow.applicationservice.activity.web;

import com.careerflow.applicationservice.activity.dto.ActivityResponse;
import com.careerflow.applicationservice.activity.service.ActivityService;
import com.careerflow.applicationservice.shared.mapper.ApplicationMapper;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ActivityController {

    private static final int DEFAULT_TIMELINE_LIMIT = 50;

    private final ActivityService activityService;
    private final ApplicationMapper applicationMapper;

    public ActivityController(ActivityService activityService, ApplicationMapper applicationMapper) {
        this.activityService = activityService;
        this.applicationMapper = applicationMapper;
    }

    @GetMapping("/activities")
    public List<ActivityResponse> getActivityTimeline(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam(defaultValue = "50") int limit
    ) {
        int effectiveLimit = limit > 0 ? Math.min(limit, 100) : DEFAULT_TIMELINE_LIMIT;
        return activityService.getUserTimeline(jwt.getSubject(), effectiveLimit).stream()
            .map(applicationMapper::toActivityResponse)
            .toList();
    }
}
