package com.finops.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiAnalysisResponse {
    private Integer resourceId;
    private String resourceName;
    private String currentArchitecture;
    private String targetArchitecture;
    private String workloadClassification;
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String confidenceScore;
    private BigDecimal estimatedMonthlySavings;
    private BigDecimal estimatedAnnualSavings;
    private String performanceImpact;
    private String technicalRationale;
    private String cliSnippet;
    private String terraformSnippet;
    private List<String> rollbackPlan;
    private List<String> architecturalBestPractices;
}
