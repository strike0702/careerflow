package com.careerflow.userservice;

import com.careerflow.common.exception.ResourceNotFoundException;
import com.careerflow.userservice.adapters.out.persistence.CandidateProfileRepository;
import com.careerflow.userservice.adapters.out.persistence.UserRepository;
import com.careerflow.userservice.domain.CandidateProfile;
import com.careerflow.userservice.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceTest extends AbstractIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CandidateProfileRepository candidateProfileRepository;

    @BeforeEach
    void cleanUp() {
        candidateProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void getOrSyncUser_createsUserOnFirstRequest() {
        Jwt jwt = Jwt.withTokenValue("user-a")
            .header("alg", "none")
            .subject("user-a")
            .claim("email", "user-a@careerflow.com")
            .claim("given_name", "Test")
            .claim("family_name", "User")
            .claim("realm_access", java.util.Map.of("roles", List.of("CANDIDATE")))
            .build();

        User user = userService.getOrSyncUser(jwt);

        assertThat(user.getId()).isEqualTo("user-a");
        assertThat(userRepository.findById("user-a")).isPresent();
        assertThat(candidateProfileRepository.findById("user-a")).isPresent();
    }

    @Test
    void updateCandidateProfile_persistsChanges() {
        User user = new User("user-a", "user-a@careerflow.com", "Test", "User", "CANDIDATE");
        userRepository.save(user);
        candidateProfileRepository.save(new CandidateProfile(user.getId()));

        CandidateProfile updated = userService.updateCandidateProfile(
            "user-a",
            "Staff Engineer",
            150000.0,
            190000.0,
            List.of("Java 21", "PostgreSQL")
        );

        assertThat(updated.getTargetRoles()).isEqualTo("Staff Engineer");
        assertThat(updated.getSkills()).containsExactly("Java 21", "PostgreSQL");
    }

    @Test
    void updateCandidateProfile_throwsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.updateCandidateProfile(
            "missing-user",
            "Staff Engineer",
            150000.0,
            190000.0,
            List.of("Java")
        )).isInstanceOf(ResourceNotFoundException.class);
    }
}
