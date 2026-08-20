package com.finops.backend.service;

import com.finops.backend.dto.CostHistoryResponse;
import com.finops.backend.dto.CreateCostRequest;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CostService {

    private final ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<UnitRepository> unitRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public CostService(ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider,
                       ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
                       ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
                       ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
                       ObjectProvider<UnitRepository> unitRepositoryProvider,
                       ObjectProvider<UserRepository> userRepositoryProvider) {
        this.costHistoryRepositoryProvider = costHistoryRepositoryProvider;
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.unitRepositoryProvider = unitRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Transactional
    public CostHistoryResponse createCost(String email, CreateCostRequest request) {
        CostHistoryRepository costHistoryRepo = costHistoryRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        UnitRepository unitRepo = unitRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (costHistoryRepo == null || resourceRepo == null || accountRepo == null ||
                serviceRepo == null || unitRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        AwsResource resource = null;
        AwsAccount account = null;
        AwsService service = null;

        // Check the constraint `chk_cost_scope`
        // 1. Resource level: resource_id IS NOT NULL AND aws_account_id IS NULL AND service_id IS NULL
        // 2. Account level: resource_id IS NULL AND aws_account_id IS NOT NULL AND service_id IS NOT NULL
        
        boolean hasResource = request.getResourceId() != null;
        boolean hasAccountAndService = request.getAwsAccountId() != null && request.getServiceName() != null;

        if (hasResource && (request.getAwsAccountId() != null || request.getServiceName() != null)) {
            throw new IllegalArgumentException("Constraint chk_cost_scope failed: For resource-level cost, account and service must be null.");
        }
        if (!hasResource && !hasAccountAndService) {
            throw new IllegalArgumentException("Constraint chk_cost_scope failed: For account-level cost, account ID and service name must be provided.");
        }

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

        Unit unit = unitRepo.findByUnitName(request.getUnitName())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));

        CostHistory costHistory = CostHistory.builder()
                .awsResource(resource)
                .awsAccount(account)
                .awsService(service)
                .usageDate(request.getUsageDate())
                .usageAmount(request.getUsageAmount())
                .unit(unit)
                .costAmount(request.getCostAmount())
                .build();

        costHistory = costHistoryRepo.save(costHistory);

        return mapToResponse(costHistory);
    }

    @Transactional(readOnly = true)
    public List<CostHistoryResponse> getCostHistory(String email, Integer accountId, Integer resourceId, LocalDate startDate, LocalDate endDate) {
        CostHistoryRepository costHistoryRepo = costHistoryRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (costHistoryRepo == null || userRepo == null) {
            return List.of();
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        List<CostHistory> costs = costHistoryRepo.findCostsForUser(email, isCompanyWide, accountId, resourceId, startDate, endDate);

        return costs.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CostHistoryResponse mapToResponse(CostHistory cost) {
        CostHistoryResponse.CostHistoryResponseBuilder builder = CostHistoryResponse.builder()
                .id(cost.getId())
                .usageDate(cost.getUsageDate())
                .usageAmount(cost.getUsageAmount())
                .unitName(cost.getUnit().getUnitName())
                .costAmount(cost.getCostAmount())
                .recordedAt(cost.getRecordedAt());

        if (cost.getAwsResource() != null) {
            builder.resourceId(cost.getAwsResource().getId())
                   .resourceName(cost.getAwsResource().getResourceName())
                   .awsAccountId(cost.getAwsResource().getAwsAccount().getId())
                   .awsAccountNumber(cost.getAwsResource().getAwsAccount().getAwsAccountNumber())
                   .serviceName(cost.getAwsResource().getResourceState().getResourceType().getService().getServiceName());
        } else {
            builder.awsAccountId(cost.getAwsAccount().getId())
                   .awsAccountNumber(cost.getAwsAccount().getAwsAccountNumber())
                   .serviceName(cost.getAwsService().getServiceName());
        }

        return builder.build();
    }
}
