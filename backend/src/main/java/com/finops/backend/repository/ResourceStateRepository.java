package com.finops.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.ResourceState;
import com.finops.backend.entity.ResourceType;

@Repository
public interface ResourceStateRepository extends JpaRepository<ResourceState, Integer> {
    Optional<ResourceState> findByStateNameAndResourceType(String stateName, ResourceType resourceType);
}
