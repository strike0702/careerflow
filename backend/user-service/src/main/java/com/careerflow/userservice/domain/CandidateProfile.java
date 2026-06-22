package com.careerflow.userservice.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidate_profiles")
public class CandidateProfile {
    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "target_roles")
    private String targetRoles;

    @Column(name = "target_salary_min")
    private Double targetSalaryMin;

    @Column(name = "target_salary_max")
    private Double targetSalaryMax;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "candidate_skills", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "skill")
    private List<String> skills;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    public CandidateProfile() {}

    public CandidateProfile(User user) {
        this.userId = user.getId();
        this.user = user;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTargetRoles() { return targetRoles; }
    public void setTargetRoles(String targetRoles) { this.targetRoles = targetRoles; }

    public Double getTargetSalaryMin() { return targetSalaryMin; }
    public void setTargetSalaryMin(Double targetSalaryMin) { this.targetSalaryMin = targetSalaryMin; }

    public Double getTargetSalaryMax() { return targetSalaryMax; }
    public void setTargetSalaryMax(Double targetSalaryMax) { this.targetSalaryMax = targetSalaryMax; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
