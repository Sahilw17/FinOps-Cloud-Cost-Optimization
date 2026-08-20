package com.finops.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.AwsCredentials;

@Repository
public interface AwsCredentialsRepository extends JpaRepository<AwsCredentials, Integer> {
}
