package com.finops.backend.service;

import com.finops.backend.dto.AlertResponse;
import com.finops.backend.dto.CreateAlertRequest;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AlertService {

    private final ObjectProvider<AlertRepository> alertRepositoryProvider;
    private final ObjectProvider<AlertTypeRepository> alertTypeRepositoryProvider;
    private final ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider;
    private final ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<UnitRepository> unitRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public AlertService(
            ObjectProvider<AlertRepository> alertRepositoryProvider,
            ObjectProvider<AlertTypeRepository> alertTypeRepositoryProvider,
            ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider,
            ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider,
            ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
            ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
            ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
            ObjectProvider<UnitRepository> unitRepositoryProvider,
            ObjectProvider<UserRepository> userRepositoryProvider) {
        this.alertRepositoryProvider = alertRepositoryProvider;
        this.alertTypeRepositoryProvider = alertTypeRepositoryProvider;
        this.resourceMetricRepositoryProvider = resourceMetricRepositoryProvider;
        this.costHistoryRepositoryProvider = costHistoryRepositoryProvider;
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.unitRepositoryProvider = unitRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Transactional
    public AlertResponse createAlert(String email, CreateAlertRequest request) {
        AlertRepository alertRepo = alertRepositoryProvider.getIfAvailable();
        AlertTypeRepository typeRepo = alertTypeRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        UnitRepository unitRepo = unitRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (alertRepo == null || typeRepo == null || resourceRepo == null ||
                accountRepo == null || serviceRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        boolean hasResource = request.getResourceId() != null;
        boolean hasAccountAndService = request.getAwsAccountId() != null && request.getServiceName() != null;

        if (hasResource && (request.getAwsAccountId() != null || request.getServiceName() != null)) {
            throw new IllegalArgumentException("Constraint chk_alert_scope failed: For resource-level alert, account and service must be null.");
        }
        if (!hasResource && !hasAccountAndService) {
            throw new IllegalArgumentException("Constraint chk_alert_scope failed: For account-level alert, account ID and service name must be provided.");
        }

        AwsResource resource = null;
        AwsAccount account = null;
        AwsService service = null;

        if (hasResource) {
            resource = isAdmin
                    ? resourceRepo.findById(request.getResourceId())
                            .orElseThrow(() -> new IllegalArgumentException("Resource not found"))
                    : resourceRepo.findByIdAndUserId(request.getResourceId(), user.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Resource not found or unauthorized"));
        } else {
            account = isAdmin
                    ? accountRepo.findById(request.getAwsAccountId())
                            .orElseThrow(() -> new IllegalArgumentException("AWS Account not found"))
                    : accountRepo.findByIdAndUser(request.getAwsAccountId(), user)
                            .orElseThrow(() -> new IllegalArgumentException("AWS Account not found or unauthorized"));
            service = serviceRepo.findByServiceName(request.getServiceName())
                    .orElseThrow(() -> new IllegalArgumentException("AWS Service not found"));
        }

        AlertType alertType = typeRepo.findByAlertType(request.getAlertType())
                .orElseGet(() -> typeRepo.save(AlertType.builder().alertType(request.getAlertType()).build()));

        Unit unit = null;
        if (request.getUnitName() != null && unitRepo != null) {
            unit = unitRepo.findByUnitName(request.getUnitName()).orElse(null);
        }

        Alert alert = Alert.builder()
                .awsResource(resource)
                .awsAccount(account)
                .awsService(service)
                .alertType(alertType)
                .title(request.getTitle())
                .message(request.getMessage())
                .thresholdValue(request.getThresholdValue())
                .actualValue(request.getActualValue())
                .unit(unit)
                .status(AlertStatus.UNREAD)
                .build();

        alert = alertRepo.save(alert);
        return mapToResponse(alert);
    }

    @Transactional
    public List<AlertResponse> detectAnomalies(String email, Integer accountId) {
        AlertRepository alertRepo = alertRepositoryProvider.getIfAvailable();
        AlertTypeRepository typeRepo = alertTypeRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        ResourceMetricRepository metricRepo = resourceMetricRepositoryProvider.getIfAvailable();
        CostHistoryRepository costRepo = costHistoryRepositoryProvider.getIfAvailable();
        UnitRepository unitRepo = unitRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (alertRepo == null || typeRepo == null || resourceRepo == null ||
                metricRepo == null || costRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        List<AlertResponse> generated = new ArrayList<>();

        AlertType costSpikeType = typeRepo.findByAlertType("COST_SPIKE_DETECTED")
                .orElseGet(() -> typeRepo.save(AlertType.builder().alertType("COST_SPIKE_DETECTED").build()));

        AlertType highUtilType = typeRepo.findByAlertType("HIGH_UTILIZATION_THRESHOLD")
                .orElseGet(() -> typeRepo.save(AlertType.builder().alertType("HIGH_UTILIZATION_THRESHOLD").build()));

        Unit usdUnit = unitRepo != null ? unitRepo.findByUnitName("USD").orElse(null) : null;
        Unit percentUnit = unitRepo != null ? unitRepo.findByUnitName("Percent").orElse(null) : null;

        // 1. Metric Anomaly Detection (High CPU breaches > 85%)
        List<AwsResource> resources = isAdmin
                ? resourceRepo.findAll()
                : resourceRepo.findAllByUserId(user.getId());

        if (accountId != null) {
            resources = resources.stream()
                    .filter(r -> r.getAwsAccount().getId().equals(accountId))
                    .collect(Collectors.toList());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        for (AwsResource res : resources) {
            List<ResourceMetric> metrics = metricRepo.findMetricsForResourceAndUser(res.getId(), email, isAdmin, sevenDaysAgo, now);
            for (ResourceMetric m : metrics) {
                if ("CPUUtilization".equalsIgnoreCase(m.getMetricName().getMetricName()) &&
                        m.getMetricValue().compareTo(new BigDecimal("85.0")) >= 0) {

                    Alert alert = Alert.builder()
                            .awsResource(res)
                            .alertType(highUtilType)
                            .title("High CPU Utilization Alert: " + res.getResourceName())
                            .message(String.format("Resource '%s' exceeded the 85%% critical CPU threshold with an observed value of %.1f%%.", res.getResourceName(), m.getMetricValue().doubleValue()))
                            .thresholdValue(new BigDecimal("85.0000"))
                            .actualValue(m.getMetricValue().setScale(4, RoundingMode.HALF_UP))
                            .unit(percentUnit)
                            .status(AlertStatus.UNREAD)
                            .build();

                    alert = alertRepo.save(alert);
                    generated.add(mapToResponse(alert));
                    break; // one alert per resource
                }
            }
        }

        // 2. Cost Spike Anomaly Detection
        LocalDate today = LocalDate.now();
        List<CostHistory> costs = costRepo.findCostsForUser(email, isAdmin, accountId, null, today.minusDays(30), today);
        if (costs.size() >= 2) {
            CostHistory latestCost = costs.get(costs.size() - 1);
            List<CostHistory> previousCosts = costs.subList(0, costs.size() - 1);

            BigDecimal avgPreviousCost = previousCosts.stream()
                    .map(CostHistory::getCostAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(previousCosts.size()), 4, RoundingMode.HALF_UP);

            BigDecimal spikeThreshold = avgPreviousCost.multiply(new BigDecimal("1.80"));

            if (latestCost.getCostAmount().compareTo(spikeThreshold) >= 0) {
                Alert.AlertBuilder alertBuilder = Alert.builder()
                        .alertType(costSpikeType)
                        .title("Cost Spike Anomaly Detected")
                        .message(String.format("Spend spiked to $%.2f which is over 1.8x the historical baseline ($%.2f).", latestCost.getCostAmount(), avgPreviousCost))
                        .thresholdValue(spikeThreshold)
                        .actualValue(latestCost.getCostAmount().setScale(4, RoundingMode.HALF_UP))
                        .unit(usdUnit)
                        .status(AlertStatus.UNREAD);

                if (latestCost.getAwsResource() != null) {
                    alertBuilder.awsResource(latestCost.getAwsResource());
                } else if (latestCost.getAwsAccount() != null) {
                    alertBuilder.awsAccount(latestCost.getAwsAccount())
                            .awsService(latestCost.getAwsService());
                }

                Alert alert = alertRepo.save(alertBuilder.build());
                generated.add(mapToResponse(alert));
            }
        }

        return generated;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> getAlerts(String email, AlertStatus status, Integer accountId, Integer resourceId) {
        AlertRepository alertRepo = alertRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (alertRepo == null || userRepo == null) {
            return List.of();
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        List<Alert> alerts = alertRepo.findAlertsForUser(email, isCompanyWide, status, accountId, resourceId);
        return alerts.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(String email) {
        AlertRepository alertRepo = alertRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (alertRepo == null || userRepo == null) {
            return 0;
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        return alertRepo.countUnreadAlertsForUser(email, isCompanyWide);
    }

    @Transactional
    public AlertResponse updateStatus(String email, Long alertId, AlertStatus status) {
        AlertRepository alertRepo = alertRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (alertRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repository not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        Alert alert = alertRepo.findByIdAndUserEmail(alertId, email, isAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found or unauthorized"));

        alert.setStatus(status);
        alert = alertRepo.save(alert);
        return mapToResponse(alert);
    }

    private AlertResponse mapToResponse(Alert alert) {
        AlertResponse.AlertResponseBuilder builder = AlertResponse.builder()
                .id(alert.getId())
                .alertType(alert.getAlertType().getAlertType())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .thresholdValue(alert.getThresholdValue())
                .actualValue(alert.getActualValue())
                .unitName(alert.getUnit() != null ? alert.getUnit().getUnitName() : null)
                .status(alert.getStatus())
                .createdAt(alert.getCreatedAt());

        if (alert.getAwsResource() != null) {
            builder.resourceId(alert.getAwsResource().getId())
                    .resourceName(alert.getAwsResource().getResourceName())
                    .awsAccountId(alert.getAwsResource().getAwsAccount().getId())
                    .awsAccountNumber(alert.getAwsResource().getAwsAccount().getAwsAccountNumber())
                    .serviceName(alert.getAwsResource().getResourceState().getResourceType().getService().getServiceName());
        } else if (alert.getAwsAccount() != null) {
            builder.awsAccountId(alert.getAwsAccount().getId())
                    .awsAccountNumber(alert.getAwsAccount().getAwsAccountNumber())
                    .serviceName(alert.getAwsService() != null ? alert.getAwsService().getServiceName() : null);
        }

        return builder.build();
    }
}
