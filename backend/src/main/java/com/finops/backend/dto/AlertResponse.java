package com.finops.backend.dto;

import com.finops.backend.entity.AlertStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;

    private Integer resourceId;

    private String resourceName;

    private Integer awsAccountId;

    private String awsAccountNumber;

    private String serviceName;

    private String alertType;

    private String title;

    private String message;

    private BigDecimal thresholdValue;

    private BigDecimal actualValue;

    private String unitName;

    private AlertStatus status;

    private LocalDateTime createdAt;
}
