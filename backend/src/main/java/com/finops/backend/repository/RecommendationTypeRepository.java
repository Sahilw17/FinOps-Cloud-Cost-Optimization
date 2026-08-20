package com.finops.backend.repository;

import com.finops.backend.entity.RecommendationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecommendationTypeRepository extends JpaRepository<RecommendationType, Integer> {
    Optional<RecommendationType> findByRecommendationType(String recommendationType);
}
