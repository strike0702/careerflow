package com.careerflow.userservice;

import com.careerflow.common.exception.ResourceNotFoundException;
import com.careerflow.userservice.adapters.out.persistence.CandidateProfileRepository;
import com.careerflow.userservice.adapters.out.persistence.UserRepository;
import com.careerflow.userservice.domain.CandidateProfile;
import com.careerflow.userservice.domain.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;

    public UserService(UserRepository userRepository, CandidateProfileRepository candidateProfileRepository) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
    }

    public User getOrSyncUser(Jwt jwt) {
        String userId = jwt.getSubject();
        Optional<User> existingUser = userRepository.findById(userId);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Parse profile details from token claims
        String email = jwt.getClaimAsString("email");
        String firstName = jwt.getClaimAsString("given_name");
        String lastName = jwt.getClaimAsString("family_name");

        // Determine highest precedence role
        String role = "CANDIDATE";
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles.contains("ADMIN")) {
                role = "ADMIN";
            }
        }

        User newUser = new User(userId, email != null ? email : "", firstName, lastName, role);
        newUser = userRepository.save(newUser);

        if ("CANDIDATE".equals(role)) {
            CandidateProfile profile = new CandidateProfile(newUser.getId());
            profile.setUpdatedAt(LocalDateTime.now());
            candidateProfileRepository.save(profile);
        }

        return newUser;
    }

    public Optional<CandidateProfile> getCandidateProfile(String userId) {
        return candidateProfileRepository.findById(userId);
    }

    public CandidateProfile updateCandidateProfile(String userId, String targetRoles, Double targetSalaryMin, Double targetSalaryMax, List<String> skills) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CandidateProfile profile = candidateProfileRepository.findById(userId)
                .orElseGet(() -> new CandidateProfile(user.getId()));

        profile.setTargetRoles(targetRoles);
        profile.setTargetSalaryMin(targetSalaryMin);
        profile.setTargetSalaryMax(targetSalaryMax);
        profile.setSkills(skills == null ? new ArrayList<>() : new ArrayList<>(skills));
        profile.setUpdatedAt(LocalDateTime.now());

        return candidateProfileRepository.save(profile);
    }
}
