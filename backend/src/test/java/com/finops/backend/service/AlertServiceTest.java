package com.finops.backend.service;

import com.finops.backend.dto.AlertResponse;
import com.finops.backend.dto.CreateAlertRequest;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AlertServiceTest {

    @Mock
    private ObjectProvider<AlertRepository> alertRepositoryProvider;
    @Mock
    private ObjectProvider<AlertTypeRepository> alertTypeRepositoryProvider;
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
    private ObjectProvider<UnitRepository> unitRepositoryProvider;
    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private AlertRepository alertRepository;
    @Mock
    private AlertTypeRepository alertTypeRepository;
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
    private UnitRepository unitRepository;
    @Mock
    private UserRepository userRepository;

    private AlertService alertService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(alertRepositoryProvider.getIfAvailable()).thenReturn(alertRepository);
        when(alertTypeRepositoryProvider.getIfAvailable()).thenReturn(alertTypeRepository);
        when(resourceMetricRepositoryProvider.getIfAvailable()).thenReturn(resourceMetricRepository);
        when(costHistoryRepositoryProvider.getIfAvailable()).thenReturn(costHistoryRepository);
        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(awsResourceRepository);
        when(awsAccountRepositoryProvider.getIfAvailable()).thenReturn(awsAccountRepository);
        when(awsServiceRepositoryProvider.getIfAvailable()).thenReturn(awsServiceRepository);
        when(unitRepositoryProvider.getIfAvailable()).thenReturn(unitRepository);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        alertService = new AlertService(
                alertRepositoryProvider,
                alertTypeRepositoryProvider,
                resourceMetricRepositoryProvider,
                costHistoryRepositoryProvider,
                awsResourceRepositoryProvider,
                awsAccountRepositoryProvider,
                awsServiceRepositoryProvider,
                unitRepositoryProvider,
                userRepositoryProvider
        );
    }

    @Test
    void testCreateAlert_ResourceLevel_Success() {
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

        AlertType alertType = mock(AlertType.class);
        when(alertType.getAlertType()).thenReturn("HIGH_UTILIZATION_THRESHOLD");

        Unit unit = mock(Unit.class);
        when(unit.getUnitName()).thenReturn("Percent");

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(resource));
        when(alertTypeRepository.findByAlertType("HIGH_UTILIZATION_THRESHOLD")).thenReturn(Optional.of(alertType));
        when(unitRepository.findByUnitName("Percent")).thenReturn(Optional.of(unit));

        Alert saved = Alert.builder()
                .id(1L)
                .awsResource(resource)
                .alertType(alertType)
                .title("High CPU Alert")
                .message("CPU exceeded 90%")
                .thresholdValue(new BigDecimal("90.00"))
                .actualValue(new BigDecimal("95.50"))
                .unit(unit)
                .status(AlertStatus.UNREAD)
                .build();

        when(alertRepository.save(any(Alert.class))).thenReturn(saved);

        CreateAlertRequest request = CreateAlertRequest.builder()
                .resourceId(1)
                .alertType("HIGH_UTILIZATION_THRESHOLD")
                .title("High CPU Alert")
                .message("CPU exceeded 90%")
                .thresholdValue(new BigDecimal("90.00"))
                .actualValue(new BigDecimal("95.50"))
                .unitName("Percent")
                .build();

        AlertResponse response = alertService.createAlert("admin@example.com", request);

        assertNotNull(response);
        assertEquals(1, response.getResourceId());
        assertEquals("TestInstance", response.getResourceName());
        assertEquals("HIGH_UTILIZATION_THRESHOLD", response.getAlertType());
        assertEquals(AlertStatus.UNREAD, response.getStatus());
    }

    @Test
    void testDetectAnomalies_HighCpu_GeneratesAlert() {
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
        when(resource.getResourceName()).thenReturn("SpikeInstance");
        when(resource.getAwsAccount()).thenReturn(account);
        when(resource.getResourceState()).thenReturn(state);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findAllByUserId(1)).thenReturn(List.of(resource));

        MetricName metricName = mock(MetricName.class);
        when(metricName.getMetricName()).thenReturn("CPUUtilization");

        ResourceMetric m1 = mock(ResourceMetric.class);
        when(m1.getMetricName()).thenReturn(metricName);
        when(m1.getMetricValue()).thenReturn(new BigDecimal("92.5"));

        when(resourceMetricRepository.findMetricsForResourceAndUser(eq(1), eq("admin@example.com"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(m1));

        AlertType alertType = mock(AlertType.class);
        when(alertType.getAlertType()).thenReturn("HIGH_UTILIZATION_THRESHOLD");
        when(alertTypeRepository.findByAlertType("HIGH_UTILIZATION_THRESHOLD")).thenReturn(Optional.of(alertType));

        when(costHistoryRepository.findCostsForUser(anyString(), any(), any(), any(), any()))
                .thenReturn(List.of());

        when(alertRepository.save(any(Alert.class))).thenAnswer(i -> {
            Alert a = i.getArgument(0);
            a.setId(55L);
            return a;
        });

        List<AlertResponse> alerts = alertService.detectAnomalies("admin@example.com", null);

        assertNotNull(alerts);
        assertEquals(1, alerts.size());
        assertTrue(alerts.get(0).getTitle().contains("High CPU"));
        assertEquals(AlertStatus.UNREAD, alerts.get(0).getStatus());
    }
}
