package com.finops.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.ResourceType;

@Repository
public interface ResourceTypeRepository extends JpaRepository<ResourceType, Integer> {
    Optional<ResourceType> findByResourceTypeName(String resourceTypeName);
}
