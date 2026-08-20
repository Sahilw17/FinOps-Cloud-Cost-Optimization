package com.finops.backend.dto;

import com.finops.backend.entity.AlertStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAlertStatusRequest {

    @NotNull(message = "Status is required")
    private AlertStatus status;
}
