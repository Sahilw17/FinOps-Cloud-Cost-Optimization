package com.finops.backend.dto;

import com.finops.backend.entity.RecommendationStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private Long id;

    private Integer resourceId;

    private String resourceName;

    private Integer awsAccountId;

    private String awsAccountNumber;

    private String serviceName;

    private String recommendationType;

    private String title;

    private String description;

    private BigDecimal estimatedMonthlySaving;

    private RecommendationStatus status;

    private LocalDateTime createdAt;
}
