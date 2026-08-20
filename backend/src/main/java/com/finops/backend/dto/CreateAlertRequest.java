package com.finops.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAlertRequest {

    private Integer resourceId;

    private Integer awsAccountId;

    private String serviceName;

    @NotBlank(message = "Alert type is required")
    private String alertType;

    @NotBlank(message = "Title is required")
    private String title;

    private String message;

    private BigDecimal thresholdValue;

    private BigDecimal actualValue;

    private String unitName;
}
