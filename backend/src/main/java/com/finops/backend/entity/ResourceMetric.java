package com.finops.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Resource_Metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "metric_id")
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private AwsResource awsResource;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "metric_name_id", nullable = false)
    private MetricName metricName;

    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "statistic", nullable = false)
    private MetricStatistic statistic;

    @Column(name = "collected_at", nullable = false)
    private LocalDateTime collectedAt;
}
