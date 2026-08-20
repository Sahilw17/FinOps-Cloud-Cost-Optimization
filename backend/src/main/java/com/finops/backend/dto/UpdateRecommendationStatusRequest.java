package com.finops.backend.dto;

import com.finops.backend.entity.RecommendationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecommendationStatusRequest {

    @NotNull(message = "Status is required")
    private RecommendationStatus status;
}
