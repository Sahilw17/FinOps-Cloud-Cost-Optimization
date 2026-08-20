package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cost_History")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cost_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id")
    private AwsResource awsResource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "aws_account_id")
    private AwsAccount awsAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "service_id")
    private AwsService awsService;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "usage_amount", precision = 15, scale = 4)
    private BigDecimal usageAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    @Column(name = "cost_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal costAmount;

    @Column(name = "recorded_at", updatable = false)
    @Builder.Default
    private LocalDateTime recordedAt = LocalDateTime.now();
}
