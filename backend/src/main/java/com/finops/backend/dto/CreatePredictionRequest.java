package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePredictionRequest {

    private Integer resourceId;

    private Integer awsAccountId;

    private String serviceName;

    @NotBlank(message = "Prediction type is required")
    private String predictionType;

    @NotNull(message = "Predicted cost is required")
    private BigDecimal predictedCost;

    private BigDecimal confidenceScore;

    @NotNull(message = "Prediction date is required")
    private LocalDate predictionDate;
}
