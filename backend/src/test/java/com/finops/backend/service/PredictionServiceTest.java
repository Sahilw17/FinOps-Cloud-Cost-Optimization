package com.finops.backend.service;

import com.finops.backend.dto.CreatePredictionRequest;
import com.finops.backend.dto.GeneratePredictionRequest;
import com.finops.backend.dto.PredictionResponse;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PredictionServiceTest {

    @Mock
    private ObjectProvider<PredictionRepository> predictionRepositoryProvider;
    @Mock
    private ObjectProvider<PredictionTypeRepository> predictionTypeRepositoryProvider;
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
    private PredictionRepository predictionRepository;
    @Mock
    private PredictionTypeRepository predictionTypeRepository;
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

    private PredictionService predictionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(predictionRepositoryProvider.getIfAvailable()).thenReturn(predictionRepository);
        when(predictionTypeRepositoryProvider.getIfAvailable()).thenReturn(predictionTypeRepository);
        when(costHistoryRepositoryProvider.getIfAvailable()).thenReturn(costHistoryRepository);
        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(awsResourceRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        when(awsServiceRepositoryProvider.getIfAvailable()).thenReturn(awsServiceRepository);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        predictionService = new PredictionService(
                predictionRepositoryProvider,
                predictionTypeRepositoryProvider,
                costHistoryRepositoryProvider,
                awsResourceRepositoryProvider,
                awsAccountRepositoryProvider,
                awsServiceRepositoryProvider,
                userRepositoryProvider
        );
    }

    @Test
    void testCreatePrediction_ResourceLevel_Success() {
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

        PredictionType predType = mock(PredictionType.class);
        when(predType.getPredictionType()).thenReturn("LINEAR_REGRESSION");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(resource));
        when(predictionTypeRepository.findByPredictionType("LINEAR_REGRESSION")).thenReturn(Optional.of(predType));

        Prediction saved = Prediction.builder()
                .id(1L)
                .awsResource(resource)
                .predictionType(predType)
                .predictedCost(new BigDecimal("120.00"))
                .confidenceScore(new BigDecimal("0.85"))
                .predictionDate(LocalDate.now().plusDays(30))
                .build();

        when(predictionRepository.save(any(Prediction.class))).thenReturn(saved);

        CreatePredictionRequest request = CreatePredictionRequest.builder()
                .resourceId(1)
                .predictionType("LINEAR_REGRESSION")
                .predictedCost(new BigDecimal("120.00"))
                .predictionDate(LocalDate.now().plusDays(30))
                .build();

        PredictionResponse response = predictionService.createPrediction("admin@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getResourceId());
        assertEquals("TestInstance", response.getResourceName());
        assertEquals(new BigDecimal("120.00"), response.getPredictedCost());
    }

    @Test
    void testGeneratePrediction_Success() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getEmail()).thenReturn("admin@example.com");

        AwsAccount account = mock(AwsAccount.class);
        when(account.getId()).thenReturn(1);
        when(account.getAwsAccountNumber()).thenReturn("123456789012");

        AwsService service = mock(AwsService.class);
        when(service.getServiceName()).thenReturn("EC2");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsAccountRepository.findByIdAndUser(1, user)).thenReturn(Optional.of(account));
        when(awsServiceRepository.findByServiceName("EC2")).thenReturn(Optional.of(service));

        PredictionType predType = mock(PredictionType.class);
        when(predType.getPredictionType()).thenReturn("LINEAR_REGRESSION");
        when(predictionTypeRepository.findByPredictionType("LINEAR_REGRESSION")).thenReturn(Optional.of(predType));

        CostHistory cost1 = mock(CostHistory.class);
        when(cost1.getCostAmount()).thenReturn(new BigDecimal("10.00"));
        CostHistory cost2 = mock(CostHistory.class);
        when(cost2.getCostAmount()).thenReturn(new BigDecimal("20.00"));

        when(costHistoryRepository.findCostsForUser(eq("admin@example.com"), eq(1), isNull(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(cost1, cost2));

        when(predictionRepository.save(any(Prediction.class))).thenAnswer(i -> {
            Prediction p = i.getArgument(0);
            p.setId(10L);
            return p;
        });

        GeneratePredictionRequest request = GeneratePredictionRequest.builder()
                .awsAccountId(1)
                .serviceName("EC2")
                .daysAhead(30)
                .predictionType("LINEAR_REGRESSION")
                .build();

        PredictionResponse response = predictionService.generatePrediction("admin@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getAwsAccountId());
        assertEquals("EC2", response.getServiceName());
        // Avg daily spend = (10+20)/2 = 15.0000. 30 days ahead = 450.00
        assertEquals(new BigDecimal("450.00"), response.getPredictedCost());
    }
}
