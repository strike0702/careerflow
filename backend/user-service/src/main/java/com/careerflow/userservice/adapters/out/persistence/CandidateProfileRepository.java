package com.careerflow.userservice.adapters.out.persistence;

import com.careerflow.userservice.domain.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, String> {
}
