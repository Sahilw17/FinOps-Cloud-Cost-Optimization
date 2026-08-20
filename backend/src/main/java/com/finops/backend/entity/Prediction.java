package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Prediction")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prediction_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private AwsResource awsResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aws_account_id")
    private AwsAccount awsAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private AwsService awsService;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "prediction_type_id", nullable = false)
    private PredictionType predictionType;

    @Column(name = "predicted_cost", precision = 15, scale = 2)
    private BigDecimal predictedCost;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "prediction_date")
    private LocalDate predictionDate;
}
