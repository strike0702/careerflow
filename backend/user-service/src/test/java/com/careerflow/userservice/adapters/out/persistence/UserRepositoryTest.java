package com.careerflow.userservice.adapters.out.persistence;

import com.careerflow.userservice.AbstractIntegrationTest;
import com.careerflow.userservice.domain.CandidateProfile;
import com.careerflow.userservice.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest extends AbstractIntegrationTest {

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
    void saveAndFindUser() {
        User user = new User("user-a", "user-a@careerflow.com", "Test", "User", "CANDIDATE");
        userRepository.save(user);

        assertThat(userRepository.findById("user-a"))
            .isPresent()
            .get()
            .extracting(User::getEmail)
            .isEqualTo("user-a@careerflow.com");
    }

    @Test
    void saveAndFindCandidateProfileWithSkills() {
        User user = new User("user-a", "user-a@careerflow.com", "Test", "User", "CANDIDATE");
        userRepository.save(user);

        CandidateProfile profile = new CandidateProfile(user.getId());
        profile.setTargetRoles("Backend Engineer");
        profile.setTargetSalaryMin(120000.0);
        profile.setTargetSalaryMax(160000.0);
        profile.setSkills(new ArrayList<>(List.of("Java", "Spring Boot")));
        candidateProfileRepository.save(profile);

        assertThat(candidateProfileRepository.findById("user-a"))
            .isPresent()
            .get()
            .extracting(CandidateProfile::getSkills)
            .asList()
            .containsExactlyInAnyOrder("Java", "Spring Boot");
    }
}
