package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCostRequest {
    private Integer resourceId; // Optional depending on scope
    private Integer awsAccountId; // Optional depending on scope
    private String serviceName; // Optional depending on scope

    @NotNull(message = "Usage date is required")
    private LocalDate usageDate;

    private BigDecimal usageAmount; // Optional

    @NotBlank(message = "Unit name is required")
    private String unitName;

    @NotNull(message = "Cost amount is required")
    private BigDecimal costAmount;
}
