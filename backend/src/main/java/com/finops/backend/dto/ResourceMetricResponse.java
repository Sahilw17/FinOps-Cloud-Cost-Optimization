package com.finops.backend.dto;

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
public class ResourceMetricResponse {
    private Long id;
    private Integer resourceId;
    private String resourceName;
    private String metricName;
    private String unitName;
    private BigDecimal metricValue;
    private String statistic;
    private LocalDateTime collectedAt;
}
