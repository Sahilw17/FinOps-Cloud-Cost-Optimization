package com.finops.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.ResourceTag;

@Repository
public interface ResourceTagRepository extends JpaRepository<ResourceTag, Long> {
}
