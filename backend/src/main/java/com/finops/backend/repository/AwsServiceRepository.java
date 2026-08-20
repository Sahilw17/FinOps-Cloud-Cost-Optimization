package com.finops.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.AwsService;

@Repository
public interface AwsServiceRepository extends JpaRepository<AwsService, Integer> {
    Optional<AwsService> findByServiceName(String serviceName);
}
