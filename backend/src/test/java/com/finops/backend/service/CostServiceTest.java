package com.finops.backend.service;

import com.finops.backend.dto.CostHistoryResponse;
import com.finops.backend.dto.CreateCostRequest;
import com.finops.backend.entity.*;
import com.finops.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CostServiceTest {

    @Mock
    private ObjectProvider<CostHistoryRepository> costHistoryRepositoryProvider;
    @Mock
    private ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    @Mock
    private ObjectProvider<AwsAccountRepository> awsAccountRepositoryProvider;
    @Mock
    private ObjectProvider<AwsServiceRepository> awsServiceRepositoryProvider;
    @Mock
    private ObjectProvider<UnitRepository> unitRepositoryProvider;
    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private CostHistoryRepository costHistoryRepository;
    @Mock
    private AwsResourceRepository awsResourceRepository;
    @Mock
    private AwsAccountRepository awsAccountRepository;
    @Mock
    private AwsServiceRepository awsServiceRepository;
    @Mock
    private UnitRepository unitRepository;
    @Mock
    private UserRepository userRepository;

    private CostService costService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(costHistoryRepositoryProvider.getIfAvailable()).thenReturn(costHistoryRepository);
        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(awsResourceRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        when(awsServiceRepositoryProvider.getIfAvailable()).thenReturn(awsServiceRepository);
        when(unitRepositoryProvider.getIfAvailable()).thenReturn(unitRepository);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        costService = new CostService(
                costHistoryRepositoryProvider,
                awsResourceRepositoryProvider,
                awsAccountRepositoryProvider,
                awsServiceRepositoryProvider,
                unitRepositoryProvider,
                userRepositoryProvider
        );
    }

    @Test
    void createCost_ResourceLevel_Success() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getEmail()).thenReturn("admin@example.com");

        AwsAccount account = mock(AwsAccount.class);
        when(account.getId()).thenReturn(1);
        when(account.getAwsAccountNumber()).thenReturn("123456789012");

        AwsService awsService = mock(AwsService.class);
        when(awsService.getServiceName()).thenReturn("EC2");

        ResourceType resourceType = mock(ResourceType.class);
        when(resourceType.getService()).thenReturn(awsService);

        ResourceState state = mock(ResourceState.class);
        when(state.getResourceType()).thenReturn(resourceType);

        AwsResource resource = mock(AwsResource.class);
        when(resource.getId()).thenReturn(1);
        when(resource.getAwsAccount()).thenReturn(account);
        when(resource.getResourceState()).thenReturn(state);
        when(resource.getResourceName()).thenReturn("TestResource");

        Unit unit = mock(Unit.class);
        when(unit.getUnitName()).thenReturn("USD");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(resource));
        when(unitRepository.findByUnitName("USD")).thenReturn(Optional.of(unit));

        CostHistory savedCost = CostHistory.builder()
                .id(1L)
                .awsResource(resource)
                .usageDate(LocalDate.now())
                .unit(unit)
                .costAmount(new BigDecimal("10.50"))
                .recordedAt(LocalDateTime.now())
                .build();

        when(costHistoryRepository.save(any(CostHistory.class))).thenReturn(savedCost);

        CreateCostRequest request = CreateCostRequest.builder()
                .resourceId(1)
                .usageDate(LocalDate.now())
                .unitName("USD")
                .costAmount(new BigDecimal("10.50"))
                .build();

        CostHistoryResponse response = costService.createCost("admin@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getResourceId());
        assertEquals("TestResource", response.getResourceName());
        assertEquals("USD", response.getUnitName());
        assertEquals(new BigDecimal("10.50"), response.getCostAmount());
    }
}
