package com.finops.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.finops.backend.entity.AwsService;
import com.finops.backend.entity.Region;
import com.finops.backend.entity.ResourceState;
import com.finops.backend.entity.ResourceType;
import com.finops.backend.entity.Role;
import com.finops.backend.entity.PredictionType;
import com.finops.backend.entity.RecommendationType;
import com.finops.backend.entity.AlertType;
import com.finops.backend.repository.AwsServiceRepository;
import com.finops.backend.repository.RegionRepository;
import com.finops.backend.repository.ResourceStateRepository;
import com.finops.backend.repository.ResourceTypeRepository;
import com.finops.backend.repository.RoleRepository;
import com.finops.backend.repository.PredictionTypeRepository;
import com.finops.backend.repository.RecommendationTypeRepository;
import com.finops.backend.repository.AlertTypeRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ObjectProvider<RoleRepository> roleRepositoryProvider;
    private final ObjectProvider<RegionRepository> regionRepositoryProvider;
    private final ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    private final ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider;
    private final ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider;
    private final ObjectProvider<com.finops.backend.repository.UnitRepository> unitRepositoryProvider;
    private final ObjectProvider<com.finops.backend.repository.MetricNameRepository> metricNameRepositoryProvider;
    private final ObjectProvider<PredictionTypeRepository> predictionTypeRepositoryProvider;
    private final ObjectProvider<RecommendationTypeRepository> recommendationTypeRepositoryProvider;
    private final ObjectProvider<AlertTypeRepository> alertTypeRepositoryProvider;

    public DataInitializer(
            ObjectProvider<RoleRepository> roleRepositoryProvider,
            ObjectProvider<RegionRepository> regionRepositoryProvider,
            ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider,
            ObjectProvider<ResourceTypeRepository> resourceTypeRepositoryProvider,
            ObjectProvider<ResourceStateRepository> resourceStateRepositoryProvider,
            ObjectProvider<com.finops.backend.repository.UnitRepository> unitRepositoryProvider,
            ObjectProvider<com.finops.backend.repository.MetricNameRepository> metricNameRepositoryProvider,
            ObjectProvider<PredictionTypeRepository> predictionTypeRepositoryProvider,
            ObjectProvider<RecommendationTypeRepository> recommendationTypeRepositoryProvider,
            ObjectProvider<AlertTypeRepository> alertTypeRepositoryProvider) {
        this.roleRepositoryProvider = roleRepositoryProvider;
        this.regionRepositoryProvider = regionRepositoryProvider;
        this.awsServiceRepositoryProvider = awsServiceRepositoryProvider;
        this.resourceTypeRepositoryProvider = resourceTypeRepositoryProvider;
        this.resourceStateRepositoryProvider = resourceStateRepositoryProvider;
        this.unitRepositoryProvider = unitRepositoryProvider;
        this.metricNameRepositoryProvider = metricNameRepositoryProvider;
        this.predictionTypeRepositoryProvider = predictionTypeRepositoryProvider;
        this.recommendationTypeRepositoryProvider = recommendationTypeRepositoryProvider;
        this.alertTypeRepositoryProvider = alertTypeRepositoryProvider;
    }

    @Override
    @Transactional
    public void run(String... args) {
        RoleRepository roleRepository = roleRepositoryProvider.getIfAvailable();
        if (roleRepository == null) {
            log.info("RoleRepository not active in current profile; skipping data initialization.");
            return;
        }

        try {
            seedRoleIfMissing(roleRepository, "ADMIN");
            seedRoleIfMissing(roleRepository, "FINOPS_ENGINEER");
            seedRoleIfMissing(roleRepository, "USER");

            seedCatalogData();
            seedPredictionAndRecommendationTypes();
            seedAlertTypes();
        } catch (Exception e) {
            log.warn("Data initialization skipped or deferred: {}", e.getMessage());
        }
    }

    private void seedRoleIfMissing(RoleRepository roleRepository, String roleName) {
        if (!roleRepository.existsByRoleName(roleName)) {
            roleRepository.save(new Role(roleName));
            log.info("Seeded initial role: {}", roleName);
        }
    }

    private void seedCatalogData() {
        RegionRepository regionRepo = regionRepositoryProvider.getIfAvailable();
        AwsServiceRepository serviceRepo = awsServiceRepositoryProvider.getIfAvailable();
        ResourceTypeRepository typeRepo = resourceTypeRepositoryProvider.getIfAvailable();
        ResourceStateRepository stateRepo = resourceStateRepositoryProvider.getIfAvailable();
        com.finops.backend.repository.UnitRepository unitRepo = unitRepositoryProvider.getIfAvailable();
        com.finops.backend.repository.MetricNameRepository metricRepo = metricNameRepositoryProvider.getIfAvailable();

        if (regionRepo == null || serviceRepo == null || typeRepo == null || stateRepo == null || unitRepo == null || metricRepo == null) return;

        // Regions
        seedRegion(regionRepo, "us-east-1", "US East N. Virginia");
        seedRegion(regionRepo, "eu-west-1", "Europe Ireland");

        // Services
        AwsService ec2 = seedService(serviceRepo, "EC2", "AWS/EC2");
        AwsService rds = seedService(serviceRepo, "RDS", "AWS/RDS");
        AwsService s3 = seedService(serviceRepo, "S3", "AWS/S3");

        // Types
        ResourceType ec2Instance = seedType(typeRepo, ec2, "ec2:instance");
        ResourceType rdsDb = seedType(typeRepo, rds, "rds:db");
        ResourceType s3Bucket = seedType(typeRepo, s3, "s3:bucket");

        // States
        seedState(stateRepo, ec2Instance, "RUNNING");
        seedState(stateRepo, ec2Instance, "STOPPED");
        seedState(stateRepo, rdsDb, "AVAILABLE");
        seedState(stateRepo, s3Bucket, "AVAILABLE");

        // Units
        com.finops.backend.entity.Unit percent = seedUnit(unitRepo, "Percent");
        com.finops.backend.entity.Unit bytes = seedUnit(unitRepo, "Bytes");
        com.finops.backend.entity.Unit count = seedUnit(unitRepo, "Count");
        com.finops.backend.entity.Unit hours = seedUnit(unitRepo, "Hours");
        com.finops.backend.entity.Unit gbMo = seedUnit(unitRepo, "GB-Mo");
        com.finops.backend.entity.Unit usd = seedUnit(unitRepo, "USD");

        // Metrics
        seedMetricName(metricRepo, "CPUUtilization", percent);
        seedMetricName(metricRepo, "MemoryUtilization", percent);
        seedMetricName(metricRepo, "NetworkIn", bytes);
        seedMetricName(metricRepo, "NetworkOut", bytes);
        seedMetricName(metricRepo, "DiskReadOps", count);
        seedMetricName(metricRepo, "DiskWriteOps", count);
    }

    private void seedPredictionAndRecommendationTypes() {
        PredictionTypeRepository predRepo = predictionTypeRepositoryProvider.getIfAvailable();
        RecommendationTypeRepository recRepo = recommendationTypeRepositoryProvider.getIfAvailable();

        if (predRepo != null) {
            seedPredictionType(predRepo, "LINEAR_REGRESSION");
            seedPredictionType(predRepo, "MOVING_AVERAGE");
            seedPredictionType(predRepo, "MONTHLY_FORECAST");
        }

        if (recRepo != null) {
            seedRecommendationType(recRepo, "RIGHTSIZE_INSTANCE");
            seedRecommendationType(recRepo, "TERMINATE_IDLE_RESOURCE");
            seedRecommendationType(recRepo, "PURCHASE_RESERVED_INSTANCE");
            seedRecommendationType(recRepo, "STORAGE_TIER_OPTIMIZATION");
        }
    }

    private void seedAlertTypes() {
        AlertTypeRepository alertRepo = alertTypeRepositoryProvider.getIfAvailable();
        if (alertRepo != null) {
            seedAlertType(alertRepo, "COST_SPIKE_DETECTED");
            seedAlertType(alertRepo, "HIGH_UTILIZATION_THRESHOLD");
            seedAlertType(alertRepo, "BUDGET_EXCEEDED");
            seedAlertType(alertRepo, "IDLE_HIGH_COST_RESOURCE");
        }
    }

    private Region seedRegion(RegionRepository repo, String code, String name) {
        return repo.findByRegionCode(code).orElseGet(() -> {
            log.info("Seeding region: {}", code);
            return repo.save(new Region(code, name));
        });
    }

    private AwsService seedService(AwsServiceRepository repo, String name, String namespace) {
        return repo.findByServiceName(name).orElseGet(() -> {
            log.info("Seeding service: {}", name);
            return repo.save(new AwsService(name, namespace));
        });
    }

    private ResourceType seedType(ResourceTypeRepository repo, AwsService service, String name) {
        return repo.findByResourceTypeName(name).orElseGet(() -> {
            log.info("Seeding resource type: {}", name);
            return repo.save(new ResourceType(service, name));
        });
    }

    private ResourceState seedState(ResourceStateRepository repo, ResourceType type, String name) {
        return repo.findByStateNameAndResourceType(name, type).orElseGet(() -> {
            log.info("Seeding resource state: {} for type {}", name, type.getResourceTypeName());
            return repo.save(new ResourceState(type, name));
        });
    }

    private com.finops.backend.entity.Unit seedUnit(com.finops.backend.repository.UnitRepository repo, String name) {
        return repo.findByUnitName(name).orElseGet(() -> {
            log.info("Seeding unit: {}", name);
            return repo.save(new com.finops.backend.entity.Unit(null, name));
        });
    }

    private com.finops.backend.entity.MetricName seedMetricName(com.finops.backend.repository.MetricNameRepository repo, String name, com.finops.backend.entity.Unit unit) {
        return repo.findByMetricName(name).orElseGet(() -> {
            log.info("Seeding metric name: {}", name);
            return repo.save(new com.finops.backend.entity.MetricName(null, name, unit));
        });
    }

    private void seedPredictionType(PredictionTypeRepository repo, String name) {
        if (repo.findByPredictionType(name).isEmpty()) {
            log.info("Seeding prediction type: {}", name);
            repo.save(PredictionType.builder().predictionType(name).build());
        }
    }

    private void seedRecommendationType(RecommendationTypeRepository repo, String name) {
        if (repo.findByRecommendationType(name).isEmpty()) {
            log.info("Seeding recommendation type: {}", name);
            repo.save(RecommendationType.builder().recommendationType(name).build());
        }
    }

    private void seedAlertType(AlertTypeRepository repo, String name) {
        if (repo.findByAlertType(name).isEmpty()) {
            log.info("Seeding alert type: {}", name);
            repo.save(AlertType.builder().alertType(name).build());
        }
    }
}
