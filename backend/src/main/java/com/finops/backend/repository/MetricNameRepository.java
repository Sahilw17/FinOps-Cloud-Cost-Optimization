package com.finops.backend.repository;

import com.finops.backend.entity.MetricName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetricNameRepository extends JpaRepository<MetricName, Integer> {
    Optional<MetricName> findByMetricName(String metricName);
}
