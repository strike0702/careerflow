package com.careerflow.interviewservice.interview.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview_retrospectives")
@Getter
@Setter
@NoArgsConstructor
public class InterviewRetrospective {

    @Id
    private UUID id;

    @Column(name = "interview_id", nullable = false, unique = true)
    private UUID interviewId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "what_went_well")
    private String whatWentWell;

    @Column(name = "what_to_improve")
    private String whatToImprove;

    @Column(name = "questions_asked")
    private String questionsAsked;

    @Column(name = "self_rating")
    private Integer selfRating;

    @Column(name = "follow_up_actions")
    private String followUpActions;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
