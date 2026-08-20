package com.finops.backend.service;

import com.finops.backend.dto.CreateMetricRequest;
import com.finops.backend.dto.ResourceMetricResponse;
import com.finops.backend.entity.AwsResource;
import com.finops.backend.entity.MetricName;
import com.finops.backend.entity.MetricStatistic;
import com.finops.backend.entity.ResourceMetric;
import com.finops.backend.repository.AwsResourceRepository;
import com.finops.backend.repository.MetricNameRepository;
import com.finops.backend.repository.ResourceMetricRepository;
import com.finops.backend.repository.UserRepository;
import com.finops.backend.entity.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MetricService {

    private final ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider;
    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<MetricNameRepository> metricNameRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public MetricService(ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider,
                         ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
                         ObjectProvider<MetricNameRepository> metricNameRepositoryProvider,
                         ObjectProvider<UserRepository> userRepositoryProvider) {
        this.resourceMetricRepositoryProvider = resourceMetricRepositoryProvider;
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.metricNameRepositoryProvider = metricNameRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Transactional
    public ResourceMetricResponse createMetric(String email, CreateMetricRequest request) {
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        MetricNameRepository metricNameRepo = metricNameRepositoryProvider.getIfAvailable();
        ResourceMetricRepository metricRepo = resourceMetricRepositoryProvider.getIfAvailable();

        if (userRepo == null || resourceRepo == null || metricNameRepo == null || metricRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        AwsResource resource = isAdmin
                ? resourceRepo.findById(request.getResourceId())
                        .orElseThrow(() -> new IllegalArgumentException("Resource not found"))
                : resourceRepo.findByIdAndUserId(request.getResourceId(), user.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Resource not found or unauthorized"));

        MetricName metricName = metricNameRepo.findByMetricName(request.getMetricName())
                .orElseThrow(() -> new IllegalArgumentException("Metric name not found: " + request.getMetricName()));

        MetricStatistic statistic;
        try {
            statistic = MetricStatistic.valueOf(request.getStatistic());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid statistic: " + request.getStatistic());
        }

        ResourceMetric resourceMetric = ResourceMetric.builder()
                .awsResource(resource)
                .metricName(metricName)
                .metricValue(request.getMetricValue())
                .statistic(statistic)
                .collectedAt(request.getCollectedAt())
                .build();

        resourceMetric = metricRepo.save(resourceMetric);

        return mapToResponse(resourceMetric);
    }

    @Transactional(readOnly = true)
    public List<ResourceMetricResponse> getMetricsForResource(String email, Integer resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        ResourceMetricRepository metricRepo = resourceMetricRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (metricRepo == null || userRepo == null) {
            return List.of();
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        List<ResourceMetric> metrics = metricRepo.findMetricsForResourceAndUser(resourceId, email, isCompanyWide, startTime, endTime);

        return metrics.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ResourceMetricResponse mapToResponse(ResourceMetric metric) {
        return ResourceMetricResponse.builder()
                .id(metric.getId())
                .resourceId(metric.getAwsResource().getId())
                .resourceName(metric.getAwsResource().getResourceName())
                .metricName(metric.getMetricName().getMetricName())
                .unitName(metric.getMetricName().getUnit().getUnitName())
                .metricValue(metric.getMetricValue())
                .statistic(metric.getStatistic().name())
                .collectedAt(metric.getCollectedAt())
                .build();
    }
}
