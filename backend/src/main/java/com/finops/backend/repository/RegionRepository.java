package com.finops.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.Region;

@Repository
public interface RegionRepository extends JpaRepository<Region, Integer> {
    Optional<Region> findByRegionCode(String regionCode);
}
