package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMetricRequest {
    @NotNull(message = "Resource ID is required")
    private Integer resourceId;

    @NotBlank(message = "Metric name is required")
    private String metricName;

    @NotNull(message = "Metric value is required")
    private BigDecimal metricValue;

    @NotBlank(message = "Statistic is required")
    private String statistic;

    @NotNull(message = "Collected at timestamp is required")
    private LocalDateTime collectedAt;
}
