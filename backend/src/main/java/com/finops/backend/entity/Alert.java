package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Alert")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
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
    @JoinColumn(name = "alert_type_id", nullable = false)
    private AlertType alertType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "threshold_value", precision = 15, scale = 4)
    private BigDecimal thresholdValue;

    @Column(name = "actual_value", precision = 15, scale = 4)
    private BigDecimal actualValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "unit_id")
    private Unit unit;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private AlertStatus status = AlertStatus.UNREAD;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
