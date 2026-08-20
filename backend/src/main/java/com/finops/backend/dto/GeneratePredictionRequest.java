package com.finops.backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePredictionRequest {

    private Integer resourceId;

    private Integer awsAccountId;

    private String serviceName;

    @Builder.Default
    private int daysAhead = 30;

    @Builder.Default
    private String predictionType = "LINEAR_REGRESSION";
}
