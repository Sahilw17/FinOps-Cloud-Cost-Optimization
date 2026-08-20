package com.finops.backend.repository;

import com.finops.backend.entity.PredictionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PredictionTypeRepository extends JpaRepository<PredictionType, Integer> {
    Optional<PredictionType> findByPredictionType(String predictionType);
}
