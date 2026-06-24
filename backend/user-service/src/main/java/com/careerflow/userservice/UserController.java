package com.careerflow.userservice;

import com.careerflow.userservice.domain.CandidateProfile;
import com.careerflow.userservice.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal Jwt jwt) {
        User user = userService.getOrSyncUser(jwt);
        return ResponseEntity.ok(new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        ));
    }

    @GetMapping("/me/profile")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        // Sync user first if not exists
        User user = userService.getOrSyncUser(jwt);
        CandidateProfile profile = userService.getCandidateProfile(user.getId())
                .orElseGet(() -> new CandidateProfile(user.getId()));

        return ResponseEntity.ok(new ProfileResponse(
                profile.getUserId(),
                profile.getTargetRoles(),
                profile.getTargetSalaryMin(),
                profile.getTargetSalaryMax(),
                profile.getSkills()
        ));
    }

    @PutMapping("/me/profile")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileUpdateRequest request) {
        
        User user = userService.getOrSyncUser(jwt);
        CandidateProfile updated = userService.updateCandidateProfile(
                user.getId(),
                request.getTargetRoles(),
                request.getTargetSalaryMin(),
                request.getTargetSalaryMax(),
                request.getSkills()
        );

        return ResponseEntity.ok(new ProfileResponse(
                updated.getUserId(),
                updated.getTargetRoles(),
                updated.getTargetSalaryMin(),
                updated.getTargetSalaryMax(),
                updated.getSkills()
        ));
    }

    // --- DTOs ---

    public record UserResponse(
            String id,
            String email,
            String firstName,
            String lastName,
            String role
    ) {}

    public record ProfileResponse(
            String userId,
            String targetRoles,
            Double targetSalaryMin,
            Double targetSalaryMax,
            List<String> skills
    ) {}

    public static class ProfileUpdateRequest {
        private String targetRoles;
        private Double targetSalaryMin;
        private Double targetSalaryMax;
        private List<String> skills;

        public ProfileUpdateRequest() {}

        public String getTargetRoles() { return targetRoles; }
        public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }

        public Double getTargetSalaryMin() { return targetSalaryMin; }
        public void setTargetSalaryMin(Double targetSalaryMin) { this.targetSalaryMin = targetSalaryMin; }

        public Double getTargetSalaryMax() { return targetSalaryMax; }
        public void setTargetSalaryMax(Double targetSalaryMax) { this.targetSalaryMax = targetSalaryMax; }

        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
    }
}
