package com.finops.backend.service;

import com.finops.backend.dto.CreateRecommendationRequest;
import com.finops.backend.dto.RecommendationResponse;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final ObjectProvider<RecommendationRepository> recommendationRepositoryProvider;
    private final ObjectProvider<RecommendationTypeRepository> recommendationTypeRepositoryProvider;
    private final ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider;
    private final ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public RecommendationService(
            ObjectProvider<RecommendationRepository> recommendationRepositoryProvider,
            ObjectProvider<RecommendationTypeRepository> recommendationTypeRepositoryProvider,
            ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider,
            ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider,
            ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
            ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
            ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
            ObjectProvider<UserRepository> userRepositoryProvider) {
        this.recommendationRepositoryProvider = recommendationRepositoryProvider;
        this.recommendationTypeRepositoryProvider = recommendationTypeRepositoryProvider;
        this.resourceMetricRepositoryProvider = resourceMetricRepositoryProvider;
        this.costHistoryRepositoryProvider = costHistoryRepositoryProvider;
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Transactional
    public RecommendationResponse createRecommendation(String email, CreateRecommendationRequest request) {
        RecommendationRepository recRepo = recommendationRepositoryProvider.getIfAvailable();
        RecommendationTypeRepository typeRepo = recommendationTypeRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (recRepo == null || typeRepo == null || resourceRepo == null ||
                accountRepo == null || serviceRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        boolean hasResource = request.getResourceId() != null;
        boolean hasAccountAndService = request.getAwsAccountId() != null && request.getServiceName() != null;

        if (hasResource && (request.getAwsAccountId() != null || request.getServiceName() != null)) {
            throw new IllegalArgumentException("Constraint chk_rec_scope failed: For resource-level recommendation, account and service must be null.");
        }
        if (!hasResource && !hasAccountAndService) {
            throw new IllegalArgumentException("Constraint chk_rec_scope failed: For account-level recommendation, account ID and service name must be provided.");
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

        RecommendationType recType = typeRepo.findByRecommendationType(request.getRecommendationType())
                .orElseGet(() -> typeRepo.save(RecommendationType.builder().recommendationType(request.getRecommendationType()).build()));

        Recommendation rec = Recommendation.builder()
                .awsResource(resource)
                .awsAccount(account)
                .awsService(service)
                .recommendationType(recType)
                .title(request.getTitle())
                .description(request.getDescription())
                .estimatedMonthlySaving(request.getEstimatedMonthlySaving() != null ? request.getEstimatedMonthlySaving() : BigDecimal.ZERO)
                .status(RecommendationStatus.NEW)
                .build();

        rec = recRepo.save(rec);
        return mapToResponse(rec);
    }

    @Transactional
    public List<RecommendationResponse> analyzeAndGenerateRecommendations(String email, Integer accountId) {
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        ResourceMetricRepository metricRepo = resourceMetricRepositoryProvider.getIfAvailable();
        RecommendationTypeRepository typeRepo = recommendationTypeRepositoryProvider.getIfAvailable();
        RecommendationRepository recRepo = recommendationRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (resourceRepo == null || accountRepo == null || serviceRepo == null ||
                metricRepo == null || typeRepo == null || recRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        List<AwsResource> resources = isAdmin
                ? resourceRepo.findAll()
                : resourceRepo.findAllByUserId(user.getId());

        if (accountId != null) {
            resources = resources.stream()
                    .filter(r -> r.getAwsAccount().getId().equals(accountId))
                    .collect(Collectors.toList());
        }

        List<RecommendationResponse> generated = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        RecommendationType rightsizeType = typeRepo.findByRecommendationType("RIGHTSIZE_INSTANCE")
                .orElseGet(() -> typeRepo.save(RecommendationType.builder().recommendationType("RIGHTSIZE_INSTANCE").build()));

        RecommendationType idleType = typeRepo.findByRecommendationType("TERMINATE_IDLE_RESOURCE")
                .orElseGet(() -> typeRepo.save(RecommendationType.builder().recommendationType("TERMINATE_IDLE_RESOURCE").build()));

        RecommendationType riType = typeRepo.findByRecommendationType("PURCHASE_RESERVED_INSTANCE")
                .orElseGet(() -> typeRepo.save(RecommendationType.builder().recommendationType("PURCHASE_RESERVED_INSTANCE").build()));

        RecommendationType storageType = typeRepo.findByRecommendationType("STORAGE_TIER_OPTIMIZATION")
                .orElseGet(() -> typeRepo.save(RecommendationType.builder().recommendationType("STORAGE_TIER_OPTIMIZATION").build()));

        // Fetch existing active recommendations to avoid duplicate insertions
        List<Recommendation> existingRecs = recRepo.findRecommendationsForUser(email, isAdmin, null, accountId, null);

        for (AwsResource resource : resources) {
            String sName = resource.getResourceState().getResourceType().getService().getServiceName();
            boolean alreadyHasRec = existingRecs.stream()
                    .anyMatch(er -> er.getAwsResource() != null &&
                            er.getAwsResource().getId().equals(resource.getId()) &&
                            (er.getStatus() == RecommendationStatus.NEW || er.getStatus() == RecommendationStatus.VIEWED));

            if (alreadyHasRec) {
                continue;
            }

            List<ResourceMetric> metrics = metricRepo.findMetricsForResourceAndUser(resource.getId(), email, isAdmin, sevenDaysAgo, now);
            List<ResourceMetric> cpuMetrics = metrics.stream()
                    .filter(m -> "CPUUtilization".equalsIgnoreCase(m.getMetricName().getMetricName()))
                    .collect(Collectors.toList());

            if (!cpuMetrics.isEmpty()) {
                double avgCpu = cpuMetrics.stream()
                        .mapToDouble(m -> m.getMetricValue().doubleValue())
                        .average()
                        .orElse(50.0);

                if (avgCpu < 10.0) {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(idleType)
                            .title("Terminate Idle Instance: " + resource.getResourceName())
                            .description(String.format("Resource '%s' has an average CPU utilization of %.1f%% over the last 7 days. Consider stopping or terminating this instance.", resource.getResourceName(), avgCpu))
                            .estimatedMonthlySaving(new BigDecimal("75.00"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                } else if (avgCpu < 30.0) {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(rightsizeType)
                            .title("Rightsize Underutilized Instance: " + resource.getResourceName())
                            .description(String.format("Resource '%s' operates at %.1f%% average CPU utilization. Downsizing to a smaller instance family will reduce monthly costs without impacting performance.", resource.getResourceName(), avgCpu))
                            .estimatedMonthlySaving(new BigDecimal("35.50"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                } else {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(riType)
                            .title("Purchase Savings Plan / RI: " + resource.getResourceName())
                            .description(String.format("Resource '%s' is heavily utilized (%.1f%% avg CPU). Switching from On-Demand to a 1-Year Compute Savings Plan will save ~35%% monthly.", resource.getResourceName(), avgCpu))
                            .estimatedMonthlySaving(new BigDecimal("28.00"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                }
            } else {
                // If resource is STOPPED or has no metrics
                if (resource.getResourceState() != null && "STOPPED".equalsIgnoreCase(resource.getResourceState().getStateName())) {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(idleType)
                            .title("Clean Up Stopped Resource: " + resource.getResourceName())
                            .description("This resource is in a STOPPED state. Verify if attached EBS volumes or reserved capacities can be released.")
                            .estimatedMonthlySaving(new BigDecimal("15.00"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                } else if ("S3".equalsIgnoreCase(sName)) {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(storageType)
                            .title("Enable S3 Intelligent-Tiering: " + resource.getResourceName())
                            .description("Activate S3 Intelligent-Tiering and lifecycle rules to automatically move infrequently accessed objects to Glacier storage tiers.")
                            .estimatedMonthlySaving(new BigDecimal("22.50"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                } else {
                    Recommendation rec = Recommendation.builder()
                            .awsResource(resource)
                            .recommendationType(rightsizeType)
                            .title("Rightsize Optimization Assessment: " + resource.getResourceName())
                            .description("Baseline resource telemetry analysis suggests evaluating memory & instance family to convert to latest generation Graviton (t4g/c6g) instances.")
                            .estimatedMonthlySaving(new BigDecimal("32.00"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                }
            }
        }

        // If no resources exist, check for linked accounts and generate account-level guidance
        if (resources.isEmpty()) {
            List<AwsAccount> accounts = isAdmin ? accountRepo.findAll() : accountRepo.findByUser(user);
            AwsService ec2Service = serviceRepo.findByServiceName("EC2").orElse(null);

            if (!accounts.isEmpty() && ec2Service != null) {
                AwsAccount primaryAccount = accounts.get(0);
                boolean accountHasRec = existingRecs.stream()
                        .anyMatch(er -> er.getAwsAccount() != null && er.getAwsAccount().getId().equals(primaryAccount.getId()));

                if (!accountHasRec) {
                    Recommendation rec = Recommendation.builder()
                            .awsAccount(primaryAccount)
                            .awsService(ec2Service)
                            .recommendationType(riType)
                            .title("Account-wide Compute Savings Plan Coverage")
                            .description("Commit to steady-state baseline usage for 1-Year Compute Savings Plans across AWS account " + primaryAccount.getAwsAccountNumber() + " to lower On-Demand rates by 30%.")
                            .estimatedMonthlySaving(new BigDecimal("120.00"))
                            .status(RecommendationStatus.NEW)
                            .build();
                    rec = recRepo.save(rec);
                    generated.add(mapToResponse(rec));
                }
            }
        }

        return generated;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(String email, RecommendationStatus status, Integer accountId, Integer resourceId) {
        RecommendationRepository recRepo = recommendationRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (recRepo == null || userRepo == null) {
            return List.of();
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        List<Recommendation> recs = recRepo.findRecommendationsForUser(email, isCompanyWide, status, accountId, resourceId);
        return recs.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public RecommendationResponse updateStatus(String email, Long recommendationId, RecommendationStatus status) {
        RecommendationRepository recRepo = recommendationRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (recRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repository not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        Recommendation rec = recRepo.findByIdAndUserEmail(recommendationId, email, isAdmin)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found or unauthorized"));

        rec.setStatus(status);
        rec = recRepo.save(rec);
        return mapToResponse(rec);
    }

    private RecommendationResponse mapToResponse(Recommendation rec) {
        RecommendationResponse.RecommendationResponseBuilder builder = RecommendationResponse.builder()
                .id(rec.getId())
                .recommendationType(rec.getRecommendationType().getRecommendationType())
                .title(rec.getTitle())
                .description(rec.getDescription())
                .estimatedMonthlySaving(rec.getEstimatedMonthlySaving())
                .status(rec.getStatus())
                .createdAt(rec.getCreatedAt());

        if (rec.getAwsResource() != null) {
            builder.resourceId(rec.getAwsResource().getId())
                    .resourceName(rec.getAwsResource().getResourceName())
                    .awsAccountId(rec.getAwsResource().getAwsAccount().getId())
                    .awsAccountNumber(rec.getAwsResource().getAwsAccount().getAwsAccountNumber())
                    .serviceName(rec.getAwsResource().getResourceState().getResourceType().getService().getServiceName());
        } else if (rec.getAwsAccount() != null) {
            builder.awsAccountId(rec.getAwsAccount().getId())
                    .awsAccountNumber(rec.getAwsAccount().getAwsAccountNumber())
                    .serviceName(rec.getAwsService() != null ? rec.getAwsService().getServiceName() : null);
        }

        return builder.build();
    }
}
