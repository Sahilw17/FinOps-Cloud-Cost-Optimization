package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecommendationRequest {

    private Integer resourceId;

    private Integer awsAccountId;

    private String serviceName;

    @NotBlank(message = "Recommendation type is required")
    private String recommendationType;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private BigDecimal estimatedMonthlySaving;
}
