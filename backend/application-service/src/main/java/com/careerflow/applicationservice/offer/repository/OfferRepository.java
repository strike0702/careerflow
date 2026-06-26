package com.careerflow.applicationservice.offer.repository;

import com.careerflow.applicationservice.offer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OfferRepository extends JpaRepository<Offer, UUID> {

    Optional<Offer> findByApplicationId(UUID applicationId);

    void deleteByApplicationId(UUID applicationId);
}
