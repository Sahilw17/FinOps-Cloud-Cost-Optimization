package com.finops.backend.service;

import com.finops.backend.dto.CreateRecommendationRequest;
import com.finops.backend.dto.RecommendationResponse;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {

    @Mock
    private ObjectProvider<RecommendationRepository> recommendationRepositoryProvider;
    @Mock
    private ObjectProvider<RecommendationTypeRepository> recommendationTypeRepositoryProvider;
    @Mock
    private ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider;
    @Mock
    private ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    @Mock
    private ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    @Mock
    private ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    @Mock
    private ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private RecommendationRepository recommendationRepository;
    @Mock
    private RecommendationTypeRepository recommendationTypeRepository;
    @Mock
    private ResourceMetricRepository resourceMetricRepository;
    @Mock
    private CostHistoryRepository costHistoryRepository;
    @Mock
    private AwsResourceRepository awsResourceRepository;
    @Mock
    private AwsAccountRepository awsAccountRepository;
    @Mock
    private AwsServiceRepository awsServiceRepository;
    @Mock
    private UserRepository userRepository;

    private RecommendationService recommendationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(recommendationRepositoryProvider.getIfAvailable()).thenReturn(recommendationRepository);
        when(recommendationTypeRepositoryProvider.getIfAvailable()).thenReturn(recommendationTypeRepository);
        when(resourceMetricRepositoryProvider.getIfAvailable()).thenReturn(resourceMetricRepository);
        when(costHistoryRepositoryProvider.getIfAvailable()).thenReturn(costHistoryRepository);
        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(awsResourceRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        when(awsServiceRepositoryProvider.getIfAvailable()).thenReturn(awsServiceRepository);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        recommendationService = new RecommendationService(
                recommendationRepositoryProvider,
                recommendationTypeRepositoryProvider,
                resourceMetricRepositoryProvider,
                costHistoryRepositoryProvider,
                awsResourceRepositoryProvider,
                awsAccountRepositoryProvider,
                awsServiceRepositoryProvider,
                userRepositoryProvider
        );
    }

    @Test
    void testCreateRecommendation_ResourceLevel_Success() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getEmail()).thenReturn("admin@example.com");

        AwsAccount account = mock(AwsAccount.class);
        when(account.getId()).thenReturn(1);
        when(account.getAwsAccountNumber()).thenReturn("123456789012");

        AwsService service = mock(AwsService.class);
        when(service.getServiceName()).thenReturn("EC2");

        ResourceType resourceType = mock(ResourceType.class);
        when(resourceType.getService()).thenReturn(service);

        ResourceState state = mock(ResourceState.class);
        when(state.getResourceType()).thenReturn(resourceType);

        AwsResource resource = mock(AwsResource.class);
        when(resource.getId()).thenReturn(1);
        when(resource.getResourceName()).thenReturn("TestInstance");
        when(resource.getAwsAccount()).thenReturn(account);
        when(resource.getResourceState()).thenReturn(state);

        RecommendationType recType = mock(RecommendationType.class);
        when(recType.getRecommendationType()).thenReturn("RIGHTSIZE_INSTANCE");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(resource));
        when(recommendationTypeRepository.findByRecommendationType("RIGHTSIZE_INSTANCE")).thenReturn(Optional.of(recType));

        Recommendation saved = Recommendation.builder()
                .id(1L)
                .awsResource(resource)
                .recommendationType(recType)
                .title("Rightsize Instance")
                .description("Instance is oversized")
                .estimatedMonthlySaving(new BigDecimal("35.00"))
                .status(RecommendationStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        when(recommendationRepository.save(any(Recommendation.class))).thenReturn(saved);

        CreateRecommendationRequest request = CreateRecommendationRequest.builder()
                .resourceId(1)
                .recommendationType("RIGHTSIZE_INSTANCE")
                .title("Rightsize Instance")
                .description("Instance is oversized")
                .estimatedMonthlySaving(new BigDecimal("35.00"))
                .build();

        RecommendationResponse response = recommendationService.createRecommendation("admin@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getResourceId());
        assertEquals("TestInstance", response.getResourceName());
        assertEquals(RecommendationStatus.NEW, response.getStatus());
        assertEquals(new BigDecimal("35.00"), response.getEstimatedMonthlySaving());
    }

    @Test
    void testAnalyzeAndGenerateRecommendations_LowCpu_GeneratesRightsize() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getEmail()).thenReturn("admin@example.com");

        AwsAccount account = mock(AwsAccount.class);
        when(account.getId()).thenReturn(1);
        when(account.getAwsAccountNumber()).thenReturn("123456789012");

        AwsService service = mock(AwsService.class);
        when(service.getServiceName()).thenReturn("EC2");

        ResourceType resourceType = mock(ResourceType.class);
        when(resourceType.getService()).thenReturn(service);

        ResourceState state = mock(ResourceState.class);
        when(state.getResourceType()).thenReturn(resourceType);

        AwsResource resource = mock(AwsResource.class);
        when(resource.getId()).thenReturn(1);
        when(resource.getResourceName()).thenReturn("IdleInstance");
        when(resource.getAwsAccount()).thenReturn(account);
        when(resource.getResourceState()).thenReturn(state);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findAllByUserId(1)).thenReturn(List.of(resource));

        MetricName metricName = mock(MetricName.class);
        when(metricName.getMetricName()).thenReturn("CPUUtilization");

        ResourceMetric m1 = mock(ResourceMetric.class);
        when(m1.getMetricName()).thenReturn(metricName);
        when(m1.getMetricValue()).thenReturn(new BigDecimal("5.0"));

        when(resourceMetricRepository.findMetricsForResourceAndUser(eq(1), eq("admin@example.com"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(m1));

        RecommendationType recType = mock(RecommendationType.class);
        when(recType.getRecommendationType()).thenReturn("TERMINATE_IDLE_RESOURCE");
        when(recommendationTypeRepository.findByRecommendationType("TERMINATE_IDLE_RESOURCE")).thenReturn(Optional.of(recType));

        when(recommendationRepository.save(any(Recommendation.class))).thenAnswer(i -> {
            Recommendation r = i.getArgument(0);
            r.setId(100L);
            return r;
        });

        List<RecommendationResponse> generated = recommendationService.analyzeAndGenerateRecommendations("admin@example.com", null);

        assertNotNull(generated);
        assertEquals(1, generated.size());
        assertTrue(generated.get(0).getTitle().contains("Terminate Idle Instance"));
        assertEquals(new BigDecimal("75.00"), generated.get(0).getEstimatedMonthlySaving());
    }
}
