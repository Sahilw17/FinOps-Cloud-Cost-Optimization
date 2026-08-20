package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Recommendation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
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
    @JoinColumn(name = "recommendation_type_id", nullable = false)
    private RecommendationType recommendationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "estimated_monthly_saving", precision = 15, scale = 2)
    private BigDecimal estimatedMonthlySaving;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private RecommendationStatus status = RecommendationStatus.NEW;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
