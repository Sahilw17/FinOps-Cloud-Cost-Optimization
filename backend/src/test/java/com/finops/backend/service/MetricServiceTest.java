package com.finops.backend.service;

import com.finops.backend.dto.CreateMetricRequest;
import com.finops.backend.dto.ResourceMetricResponse;
import com.finops.backend.entity.*;
import com.finops.backend.repository.AwsResourceRepository;
import com.finops.backend.repository.MetricNameRepository;
import com.finops.backend.repository.ResourceMetricRepository;
import com.finops.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.ObjectProvider;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MetricServiceTest {

    @Mock
    private ObjectProvider<ResourceMetricRepository> resourceMetricRepositoryProvider;
    @Mock
    private ObjectProvider<AwsResourceRepository> awsResourceRepositoryProvider;
    @Mock
    private ObjectProvider<MetricNameRepository> metricNameRepositoryProvider;
    @Mock
    private ObjectProvider<UserRepository> userRepositoryProvider;

    @Mock
    private ResourceMetricRepository resourceMetricRepository;
    @Mock
    private AwsResourceRepository awsResourceRepository;
    @Mock
    private MetricNameRepository metricNameRepository;
    @Mock
    private UserRepository userRepository;

    private MetricService metricService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(resourceMetricRepositoryProvider.getIfAvailable()).thenReturn(resourceMetricRepository);
        when(awsResourceRepositoryProvider.getIfAvailable()).thenReturn(awsResourceRepository);
        when(metricNameRepositoryProvider.getIfAvailable()).thenReturn(metricNameRepository);
        when(userRepositoryProvider.getIfAvailable()).thenReturn(userRepository);

        metricService = new MetricService(
                resourceMetricRepositoryProvider,
                awsResourceRepositoryProvider,
                metricNameRepositoryProvider,
                userRepositoryProvider
        );
    }

    @Test
    void createMetric_Success() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        when(user.getEmail()).thenReturn("admin@example.com");

        AwsResource resource = mock(AwsResource.class);
        when(resource.getId()).thenReturn(1);
        when(resource.getResourceName()).thenReturn("TestResource");

        Unit unit = mock(Unit.class);
        when(unit.getUnitName()).thenReturn("Percent");

        MetricName metricName = mock(MetricName.class);
        when(metricName.getMetricName()).thenReturn("CPUUtilization");
        when(metricName.getUnit()).thenReturn(unit);

        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(awsResourceRepository.findByIdAndUserId(1, 1)).thenReturn(Optional.of(resource));
        when(metricNameRepository.findByMetricName("CPUUtilization")).thenReturn(Optional.of(metricName));

        ResourceMetric savedMetric = ResourceMetric.builder()
                .id(1L)
                .awsResource(resource)
                .metricName(metricName)
                .metricValue(new BigDecimal("50.5"))
                .statistic(MetricStatistic.Average)
                .collectedAt(LocalDateTime.now())
                .build();

        when(resourceMetricRepository.save(any(ResourceMetric.class))).thenReturn(savedMetric);

        CreateMetricRequest request = CreateMetricRequest.builder()
                .resourceId(1)
                .metricName("CPUUtilization")
                .metricValue(new BigDecimal("50.5"))
                .statistic("Average")
                .collectedAt(LocalDateTime.now())
                .build();

        ResourceMetricResponse response = metricService.createMetric("admin@example.com", request);

        assertNotNull(response);
        assertEquals("CPUUtilization", response.getMetricName());
        assertEquals("Percent", response.getUnitName());
        assertEquals(new BigDecimal("50.5"), response.getMetricValue());
    }
}
