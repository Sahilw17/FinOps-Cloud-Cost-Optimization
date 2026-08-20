package com.finops.backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResponse {

    private Long id;

    private Integer resourceId;

    private String resourceName;

    private Integer awsAccountId;

    private String awsAccountNumber;

    private String serviceName;

    private String predictionType;

    private BigDecimal predictedCost;

    private BigDecimal confidenceScore;

    private LocalDate predictionDate;
}
