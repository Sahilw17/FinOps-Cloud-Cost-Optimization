package com.finops.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finops.backend.entity.AwsResource;

@Repository
public interface AwsResourceRepository extends JpaRepository<AwsResource, Integer> {
    
    // Find a specific resource, ensuring it belongs to one of the user's accounts
    @Query("SELECT r FROM AwsResource r WHERE r.id = :resourceId AND r.awsAccount.user.id = :userId")
    Optional<AwsResource> findByIdAndUserId(@Param("resourceId") Integer resourceId, @Param("userId") Integer userId);

    // Find all resources for a specific user
    @Query("SELECT r FROM AwsResource r WHERE r.awsAccount.user.id = :userId")
    List<AwsResource> findAllByUserId(@Param("userId") Integer userId);

    // Ensure uniqueness check for resource ARN
    Optional<AwsResource> findByResourceArn(String resourceArn);
}
