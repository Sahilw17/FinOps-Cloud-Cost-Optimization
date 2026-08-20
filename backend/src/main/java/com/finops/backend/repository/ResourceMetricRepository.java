package com.finops.backend.repository;

import com.finops.backend.entity.ResourceMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResourceMetricRepository extends JpaRepository<ResourceMetric, Long> {
    
    @Query("SELECT rm FROM ResourceMetric rm " +
           "WHERE (:isAdmin = true OR rm.awsResource.awsAccount.user.email = :email) " +
           "AND (:resourceId IS NULL OR rm.awsResource.id = :resourceId) " +
           "AND rm.collectedAt BETWEEN :startTime AND :endTime " +
           "ORDER BY rm.collectedAt ASC")
    List<ResourceMetric> findMetricsForResourceAndUser(
            @Param("resourceId") Integer resourceId,
            @Param("email") String email,
            @Param("isAdmin") boolean isAdmin,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);
}
