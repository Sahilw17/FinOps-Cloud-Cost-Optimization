package com.finops.backend.service;

import com.finops.backend.dto.CreatePredictionRequest;
import com.finops.backend.dto.GeneratePredictionRequest;
import com.finops.backend.dto.PredictionResponse;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PredictionService {

    private final ObjectProvider<PredictionRepository> predictionRepositoryProvider;
    private final ObjectProvider<PredictionTypeRepository> predictionTypeRepositoryProvider;
    private final ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    private final ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    private final ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<UserRepository> userRepositoryProvider;

    public PredictionService(
            ObjectProvider<PredictionRepository> predictionRepositoryProvider,
            ObjectProvider<PredictionTypeRepository> predictionTypeRepositoryProvider,
            ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider,
            ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider,
            ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider,
            ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
            ObjectProvider<UserRepository> userRepositoryProvider) {
        this.predictionRepositoryProvider = predictionRepositoryProvider;
        this.predictionTypeRepositoryProvider = predictionTypeRepositoryProvider;
        this.costHistoryRepositoryProvider = costHistoryRepositoryProvider;
        this.awsResourceRepositoryProvider = awsResourceRepositoryProvider;
        this.awsAccountRepositoryProvider = awsAccountRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.userRepositoryProvider = userRepositoryProvider;
    }

    @Transactional
    public PredictionResponse createPrediction(String email, CreatePredictionRequest request) {
        PredictionRepository predictionRepo = predictionRepositoryProvider.getIfAvailable();
        PredictionTypeRepository typeRepo = predictionTypeRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (predictionRepo == null || typeRepo == null || resourceRepo == null ||
                accountRepo == null || serviceRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        boolean hasResource = request.getResourceId() != null;
        boolean hasAccountAndService = request.getAwsAccountId() != null && request.getServiceName() != null;

        if (hasResource && (request.getAwsAccountId() != null || request.getServiceName() != null)) {
            throw new IllegalArgumentException("Constraint chk_prediction_scope failed: For resource-level prediction, account and service must be null.");
        }
        if (!hasResource && !hasAccountAndService) {
            throw new IllegalArgumentException("Constraint chk_prediction_scope failed: For account-level prediction, account ID and service name must be provided.");
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

        PredictionType predictionType = typeRepo.findByPredictionType(request.getPredictionType())
                .orElseThrow(() -> new IllegalArgumentException("Prediction type not found: " + request.getPredictionType()));

        Prediction prediction = Prediction.builder()
                .awsResource(resource)
                .awsAccount(account)
                .awsService(service)
                .predictionType(predictionType)
                .predictedCost(request.getPredictedCost())
                .confidenceScore(request.getConfidenceScore() != null ? request.getConfidenceScore() : new BigDecimal("0.85"))
                .predictionDate(request.getPredictionDate())
                .build();

        prediction = predictionRepo.save(prediction);
        return mapToResponse(prediction);
    }

    @Transactional
    public PredictionResponse generatePrediction(String email, GeneratePredictionRequest request) {
        CostHistoryRepository costHistoryRepo = costHistoryRepositoryProvider.getIfAvailable();
        PredictionTypeRepository typeRepo = predictionTypeRepositoryProvider.getIfAvailable();
        PredictionRepository predictionRepo = predictionRepositoryProvider.getIfAvailable();
        AwsResourceRepository resourceRepo = awsResourceRepositoryProvider.getIfAvailable();
        AwsAccountRepository accountRepo = awsAccountRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();

        if (costHistoryRepo == null || typeRepo == null || predictionRepo == null ||
                resourceRepo == null || accountRepo == null || serviceRepo == null || userRepo == null) {
            throw new IllegalStateException("Database repositories are not available");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getRoleName());

        boolean hasResource = request.getResourceId() != null;
        boolean hasAccountAndService = request.getAwsAccountId() != null && request.getServiceName() != null;

        if (hasResource && (request.getAwsAccountId() != null || request.getServiceName() != null)) {
            throw new IllegalArgumentException("Constraint chk_prediction_scope failed: For resource-level prediction, account and service must be null.");
        }
        if (!hasResource && !hasAccountAndService) {
            throw new IllegalArgumentException("Constraint chk_prediction_scope failed: For account-level prediction, account ID and service name must be provided.");
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

        // Retrieve historical costs over the last 90 days
        LocalDate now = LocalDate.now();
        List<CostHistory> history = costHistoryRepo.findCostsForUser(
                email,
                isAdmin,
                account != null ? account.getId() : null,
                resource != null ? resource.getId() : null,
                now.minusDays(90),
                now
        );

        BigDecimal predictedDailySpend;
        BigDecimal confidence;

        if (!history.isEmpty()) {
            BigDecimal totalSpend = history.stream()
                    .map(CostHistory::getCostAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            predictedDailySpend = totalSpend.divide(BigDecimal.valueOf(history.size()), 4, RoundingMode.HALF_UP);
            confidence = BigDecimal.valueOf(Math.min(0.95, 0.60 + (history.size() * 0.05)))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            predictedDailySpend = new BigDecimal("10.00");
            confidence = new BigDecimal("0.50");
        }

        int daysAhead = request.getDaysAhead() > 0 ? request.getDaysAhead() : 30;
        BigDecimal predictedTotalCost = predictedDailySpend.multiply(BigDecimal.valueOf(daysAhead))
                .setScale(2, RoundingMode.HALF_UP);

        String typeStr = request.getPredictionType() != null ? request.getPredictionType() : "LINEAR_REGRESSION";
        PredictionType predictionType = typeRepo.findByPredictionType(typeStr)
                .orElseGet(() -> typeRepo.save(PredictionType.builder().predictionType(typeStr).build()));

        Prediction prediction = Prediction.builder()
                .awsResource(resource)
                .awsAccount(account)
                .awsService(service)
                .predictionType(predictionType)
                .predictedCost(predictedTotalCost)
                .confidenceScore(confidence)
                .predictionDate(now.plusDays(daysAhead))
                .build();

        prediction = predictionRepo.save(prediction);
        return mapToResponse(prediction);
    }

    @Transactional(readOnly = true)
    public List<PredictionResponse> getPredictions(String email, Integer accountId, Integer resourceId, LocalDate startDate, LocalDate endDate) {
        PredictionRepository predictionRepo = predictionRepositoryProvider.getIfAvailable();
        UserRepository userRepo = userRepositoryProvider.getIfAvailable();
        if (predictionRepo == null || userRepo == null) {
            return List.of();
        }

        User user = userRepo.findByEmail(email).orElse(null);
        boolean isCompanyWide = user != null && user.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(user.getRole().getRoleName()) || "USER".equalsIgnoreCase(user.getRole().getRoleName()));

        List<Prediction> predictions = predictionRepo.findPredictionsForUser(email, isCompanyWide, accountId, resourceId, startDate, endDate);
        return predictions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private PredictionResponse mapToResponse(Prediction prediction) {
        PredictionResponse.PredictionResponseBuilder builder = PredictionResponse.builder()
                .id(prediction.getId())
                .predictionType(prediction.getPredictionType().getPredictionType())
                .predictedCost(prediction.getPredictedCost())
                .confidenceScore(prediction.getConfidenceScore())
                .predictionDate(prediction.getPredictionDate());

        if (prediction.getAwsResource() != null) {
            builder.resourceId(prediction.getAwsResource().getId())
                    .resourceName(prediction.getAwsResource().getResourceName())
                    .awsAccountId(prediction.getAwsResource().getAwsAccount().getId())
                    .awsAccountNumber(prediction.getAwsResource().getAwsAccount().getAwsAccountNumber())
                    .serviceName(prediction.getAwsResource().getResourceState().getResourceType().getService().getServiceName());
        } else if (prediction.getAwsAccount() != null) {
            builder.awsAccountId(prediction.getAwsAccount().getId())
                    .awsAccountNumber(prediction.getAwsAccount().getAwsAccountNumber())
                    .serviceName(prediction.getAwsService() != null ? prediction.getAwsService().getServiceName() : null);
        }

        return builder.build();
    }
}
