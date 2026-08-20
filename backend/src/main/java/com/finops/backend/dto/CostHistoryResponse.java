package com.finops.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostHistoryResponse {
    private Long id;
    private Integer resourceId;
    private String resourceName;
    private Integer awsAccountId;
    private String awsAccountNumber;
    private String serviceName;
    private LocalDate usageDate;
    private BigDecimal usageAmount;
    private String unitName;
    private BigDecimal costAmount;
    private LocalDateTime recordedAt;
}
